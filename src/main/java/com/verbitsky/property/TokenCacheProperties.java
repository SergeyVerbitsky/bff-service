package com.verbitsky.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("token-cache")
public record TokenCacheProperties(
        int recordTtl,
        int initialSize,
        int maxSize,
        int concurrencyLevel) {
}
