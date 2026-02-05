package com.example.metricsdemo.service;

import com.example.metricsdemo.model.User;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class UserCacheService {

    private static final Logger logger = LoggerFactory.getLogger(UserCacheService.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final Counter cacheHitCounter;
    private final Counter cacheMissCounter;

    private static final String USER_CACHE_PREFIX = "user:";
    private static final long CACHE_TTL_HOURS = 24;

    public UserCacheService(MeterRegistry meterRegistry) {
        this.cacheHitCounter = Counter.builder("user_cache_hits_total")
                .description("Total number of user cache hits")
                .register(meterRegistry);
        
        this.cacheMissCounter = Counter.builder("user_cache_misses_total")
                .description("Total number of user cache misses")
                .register(meterRegistry);
    }

    public void cacheUser(User user) {
        String key = USER_CACHE_PREFIX + user.getId();
        redisTemplate.opsForValue().set(key, user, CACHE_TTL_HOURS, TimeUnit.HOURS);
    }

    public User getCachedUser(Long userId) {
        String key = USER_CACHE_PREFIX + userId;
        User cachedUser = (User) redisTemplate.opsForValue().get(key);
        
        if (cachedUser != null) {
            logger.info("Cache HIT - Fetching user {} from Redis", userId);
            cacheHitCounter.increment();
            return cachedUser;
        } else {
            logger.info("Cache MISS - User {} not found in Redis", userId);
            cacheMissCounter.increment();
            return null;
        }
    }

    public void evictUser(Long userId) {
        String key = USER_CACHE_PREFIX + userId;
        redisTemplate.delete(key);
    }

    public void evictAllUsers() {
        redisTemplate.delete(redisTemplate.keys(USER_CACHE_PREFIX + "*"));
    }
}