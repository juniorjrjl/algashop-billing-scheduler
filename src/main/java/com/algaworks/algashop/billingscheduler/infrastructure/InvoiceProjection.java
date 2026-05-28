package com.algaworks.algashop.billingscheduler.infrastructure;

import java.util.UUID;

public record InvoiceProjection(UUID id, String paymentGatewayCode) {

}
