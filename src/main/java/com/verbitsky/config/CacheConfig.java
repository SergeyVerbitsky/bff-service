package com.verbitsky.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.verbitsky.security.CustomUserDetails;
import com.verbitsky.property.TokenCacheProperties;

@Configuration
@EnableConfigurationProperties(TokenCacheProperties.class)
public class CacheConfig {
    private final TokenCacheProperties cacheProperties;

    public CacheConfig(TokenCacheProperties cacheProperties) {
        this.cacheProperties = cacheProperties;
    }

    @Bean("tokenCache")
    public Cache<String, CustomUserDetails> tokenCache() {
        return CacheBuilder.newBuilder()
                .initialCapacity(cacheProperties.initialSize())
                .expireAfterWrite(cacheProperties.recordTtl(), TimeUnit.MINUTES)
                .concurrencyLevel(cacheProperties.concurrencyLevel())
                .maximumSize(cacheProperties.maxSize())
                .build();
    }
}
