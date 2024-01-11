package com.verbitsky.storage;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.cache.Cache;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RedisCacheInitializer {
    private final RedisCacheManager cacheManager;

    public RedisCacheInitializer(RedisCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @EventListener(ApplicationStartedEvent.class)
    public void updateCacheOnStartup() {
        log.info("Run startup cache invalidation");
        cacheManager.getCacheNames().stream()
                .map(cacheManager::getCache)
                .forEach(Cache::invalidate);
    }
}
