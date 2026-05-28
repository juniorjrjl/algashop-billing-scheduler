package com.algaworks.algashop.billingscheduler.infrastructure;

import com.algaworks.algashop.billingscheduler.application.CancelExpiredInvoiceApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CancelExpiredInvoiceApplicationServiceJDBCImpl implements CancelExpiredInvoiceApplicationService {

    private final JdbcOperations jdbcOperations;
    private final TransactionTemplate transactionTemplate;
    private final FastpayPaymentAPIClient fastpayPaymentAPIClient;

    private static final Duration EXPIRED_SINCE = Duration.ofDays(1);

    private static final String UNPAID_STATUS = "UNPAID";
    private static final String CANCEL_STATUS = "CANCELED";
    private static final String CANCEL_REASON = "Invoice expired";
    private static final int BATCH_SIZE = 50;

    private static final String SELECT_EXPIRED_INVOICES_SQL = String.format("""
            select i.id,
                   ps.gateway_code
              from invoice i
             inner join payment_settings ps
                on i.payment_settings_id = ps.id
             where i.expires_at <= NOW() - INTERVAL '%d days'
               and i.status = ?
             order by i.expires_at asc
             limit ?
               for update
              skip locked
            """, EXPIRED_SINCE.toDays());

    private static final String UPDATE_INVOICE_STATUS_SQL = """
            update invoice set
                   status = ?,
                   canceled_at = NOW(),
                   cancel_reason = ?
             where id = ?
            """;

    @Override
    public void cancelExpiredInvoice() {
        transactionTemplate.execute(status -> {
            final var invoiceIds = fetchExpiredInvoices();
            log.info("Task - Total invoices fetched: {}", invoiceIds.size());
            if (invoiceIds.isEmpty()) {
                log.info("Task - No expired invoices");
            }
            final var totalCancelled = cancelInvoices(invoiceIds);
            log.info("Task - Total cancelled invoices: {}", totalCancelled);
            return true;
        });
    }

    private List<InvoiceProjection> fetchExpiredInvoices() {
        final PreparedStatementSetter preparedStatementSetter = ps -> {
            ps.setString(1, UNPAID_STATUS);
            ps.setInt(2, BATCH_SIZE);
        };
        final RowMapper<InvoiceProjection> rowMapper = (rs, _) ->
            new InvoiceProjection(
                    rs.getObject("id", UUID.class),
                    rs.getString("gateway_code")
            );
        return jdbcOperations.query(SELECT_EXPIRED_INVOICES_SQL,preparedStatementSetter, rowMapper);
    }

    private int cancelInvoices(final List<InvoiceProjection> invoice) {
        final var canceledInvoices = invoice.stream().filter(this::paymentGatewayCancel)
                .map(InvoiceProjection::id)
                .toList();
            try {
                jdbcOperations.batchUpdate(UPDATE_INVOICE_STATUS_SQL,
                        canceledInvoices,
                        canceledInvoices.size(),
                        (ps, id) -> {
                            ps.setString(1, CANCEL_STATUS);
                            ps.setString(2, CANCEL_REASON);
                            ps.setObject(3, id);
                        }
                        );
                log.info("Task - Invoices cancelled {}", canceledInvoices);
                return canceledInvoices.size();
            }catch (DataAccessException e) {
                log.error("Task - Failed to cancel expired invoices {}", canceledInvoices, e);
            }
        return 0;
    }

    private boolean paymentGatewayCancel(final InvoiceProjection invoice) {
        try {
            fastpayPaymentAPIClient.cancel(invoice.paymentGatewayCode());
            log.info("Task - Invoice {} has the payment cancelled on gateway", invoice);
            return true;
        } catch (Exception _) {
            log.error("Task - Fail to cancel invoice {} on gateway", invoice);
            return false;
        }
    }

}
