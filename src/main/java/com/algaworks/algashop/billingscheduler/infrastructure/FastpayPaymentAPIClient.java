package com.algaworks.algashop.billingscheduler.infrastructure;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PutExchange;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@HttpExchange(value = "/api/v1/payments", accept = APPLICATION_JSON_VALUE)
public interface FastpayPaymentAPIClient {

    @PutExchange("/{paymentId}/cancel")
    void cancel(@PathVariable String paymentId);

}
