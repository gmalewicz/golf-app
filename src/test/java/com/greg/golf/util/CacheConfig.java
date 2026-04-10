package com.greg.golf.util;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class CacheConfig {

    @SuppressWarnings("unused")
    @Bean
    CacheManager cacheManager() {
        return new NoOpCacheManager();
    }
}
