package com.verbitsky.config;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.verbitsky.property.TokenCacheProperties;
import com.verbitsky.security.CustomUserDetails;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties(TokenCacheProperties.class)
class CacheConfig {
    private final TokenCacheProperties cacheProperties;

    public CacheConfig(TokenCacheProperties cacheProperties) {
        this.cacheProperties = cacheProperties;
    }

    @Bean("tokenCache")
    Cache<String, CustomUserDetails> tokenCache() {
        return CacheBuilder.newBuilder()
                .initialCapacity(cacheProperties.initialSize())
                .expireAfterWrite(cacheProperties.recordTtl(), TimeUnit.MINUTES)
                .concurrencyLevel(cacheProperties.concurrencyLevel())
                .maximumSize(cacheProperties.maxSize())
                .build();
    }
}
