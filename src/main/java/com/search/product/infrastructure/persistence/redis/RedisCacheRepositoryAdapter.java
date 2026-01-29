package com.search.product.infrastructure.persistence.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.search.product.domain.port.outbound.CacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Redis Implementation of CacheRepository
 * Provides distributed caching capabilities
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisCacheRepositoryAdapter implements CacheRepository {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    
    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        try {
            String value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return Optional.empty();
            }
            
            T object = objectMapper.readValue(value, type);
            return Optional.of(object);
            
        } catch (JsonProcessingException e) {
            log.error("Error deserializing cache value for key: {}", key, e);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error getting value from cache for key: {}", key, e);
            return Optional.empty();
        }
    }
    
    @Override
    public <T> void put(String key, T value) {
        try {
            String jsonValue = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, jsonValue);
        } catch (JsonProcessingException e) {
            log.error("Error serializing cache value for key: {}", key, e);
        } catch (Exception e) {
            log.error("Error putting value in cache for key: {}", key, e);
        }
    }
    
    @Override
    public <T> void put(String key, T value, long ttlSeconds) {
        try {
            String jsonValue = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, jsonValue, ttlSeconds, TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            log.error("Error serializing cache value for key: {}", key, e);
        } catch (Exception e) {
            log.error("Error putting value in cache with TTL for key: {}", key, e);
        }
    }
    
    @Override
    public void evict(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Error evicting cache for key: {}", key, e);
        }
    }
    
    @Override
    public void clear() {
        try {
            redisTemplate.getConnectionFactory().getConnection().flushAll();
        } catch (Exception e) {
            log.error("Error clearing all cache", e);
        }
    }
    
    @Override
    public boolean exists(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Error checking key existence: {}", key, e);
            return false;
        }
    }
}
