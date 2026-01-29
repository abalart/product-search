package com.search.product.domain.port.outbound;

import java.util.Optional;

/**
 * Port for Cache Repository
 * Abstracts caching operations
 */
public interface CacheRepository {
    
    /**
     * Get value from cache
     */
    <T> Optional<T> get(String key, Class<T> type);
    
    /**
     * Put value in cache
     */
    <T> void put(String key, T value);
    
    /**
     * Put value in cache with TTL (seconds)
     */
    <T> void put(String key, T value, long ttlSeconds);
    
    /**
     * Remove value from cache
     */
    void evict(String key);
    
    /**
     * Clear all cache
     */
    void clear();
    
    /**
     * Check if key exists in cache
     */
    boolean exists(String key);
}
