package com.verbitsky.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import com.verbitsky.property.CacheProperties;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static com.verbitsky.storage.CacheConstants.getCacheNames;

@Configuration
@EnableJpaRepositories(basePackages = "com.verbitsky.storage")
@EnableCaching
@EnableJpaAuditing(dateTimeProviderRef = "utcDateTimeProvider")
@EntityScan(basePackages = "com.verbitsky.entity")
@EnableConfigurationProperties(CacheProperties.class)
public class StorageConfig {
    @Bean
    RedisCacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory, CacheProperties cacheProperties) {

        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig();
        RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultCacheConfig.entryTtl(cacheProperties.timeToLive()));

        if (cacheProperties.enableStatistics()) {
            builder.enableStatistics();
        }

        if (!cacheProperties.cacheNullValues()) {
            builder.disableCreateOnMissingCache();
        }

        getCacheNames().forEach(cacheName -> builder.withCacheConfiguration(cacheName, defaultCacheConfig));

        return builder.build();
    }

    @Bean
    public DateTimeProvider utcDateTimeProvider() {
        return () -> Optional.of(LocalDateTime.now(ZoneId.systemDefault()));
    }
}
