package com.verbitsky.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("spring.cache.redis")
public record CacheProperties(
        boolean cacheNullValues,
        Duration timeToLive,
        boolean enableStatistics) {
}
