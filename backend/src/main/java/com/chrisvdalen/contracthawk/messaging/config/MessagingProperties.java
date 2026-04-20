package com.chrisvdalen.contracthawk.messaging.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "contracthawk.messaging")
public record MessagingProperties(
        String exchange,
        String queue,
        String routingKey,
        String deadLetterExchange,
        String deadLetterQueue,
        String deadLetterRoutingKey,
        Retry retry) {

    public record Retry(int maxAttempts, long initialIntervalMs, double multiplier, long maxIntervalMs) {
    }
}
