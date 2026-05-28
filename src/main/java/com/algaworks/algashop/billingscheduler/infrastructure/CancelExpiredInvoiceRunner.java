package com.algaworks.algashop.billingscheduler.infrastructure;

import com.algaworks.algashop.billingscheduler.application.CancelExpiredInvoiceApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CancelExpiredInvoiceRunner implements ApplicationRunner {

    private final CancelExpiredInvoiceApplicationService applicationService;

    @Override
    public void run(final ApplicationArguments args) throws Exception {
        log.info("Task started - Cancelling expired invoices");
        applicationService.cancelExpiredInvoice();
        log.info("Task ended - Expired invoices");
    }
}
