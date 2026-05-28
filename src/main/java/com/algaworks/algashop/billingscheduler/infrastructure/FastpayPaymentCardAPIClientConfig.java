package com.algaworks.algashop.billingscheduler.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class FastpayPaymentCardAPIClientConfig {

    @Bean
    FastpayPaymentAPIClient fastpayPaymentAPIClient(final RestClient.Builder builder,
                                                    final AlgashopPaymentProperties properties) {
        final var fastpayProperties = properties.fastpay();
        final var restClient = builder.baseUrl(fastpayProperties.hostname())
                .requestInterceptor(((request, body, execution) -> {
                    request.getHeaders().add("Token", fastpayProperties.privateToken());
                    return execution.execute(request, body);
                })).build();
        final var adapter = RestClientAdapter.create(restClient);
        final var proxyFactory = HttpServiceProxyFactory.builderFor(adapter).build();
        return proxyFactory.createClient(FastpayPaymentAPIClient.class);
    }
}
