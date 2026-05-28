package com.algaworks.algashop.billingscheduler.infrastructure;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("algashop.integrations.payment")
@Validated
public record AlgashopPaymentProperties(
        @NotNull
        FastPayProperties fastpay
) {


    @Validated
    public record FastPayProperties(
            @NotBlank
            String hostname,
            @NotBlank
            String privateToken
    ){}

}