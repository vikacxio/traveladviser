package com.kahanchale.traveladviser.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    /**
     * Main RestTemplate bean
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate(clientHttpRequestFactory());
    }

    /**
     * Custom request factory with timeouts
     */
    private ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        // Timeouts (in milliseconds)
        factory.setConnectTimeout(5000);   // 5 seconds
        factory.setReadTimeout(10000);     // 10 seconds

        // Buffering wrapper (needed if you later add logging interceptors)
        return new BufferingClientHttpRequestFactory(factory);
    }
}