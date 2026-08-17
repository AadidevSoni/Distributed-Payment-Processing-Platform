package com.visasim.userservice.service;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class IdempotencyService {

    private static final String KEY_PREFIX = "idempotency:";
    private static final Duration TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;

    public IdempotencyService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Atomically checks-and-marks a key as seen.
     * Returns true if this is the FIRST time we've seen this key
     * (caller should proceed). Returns false if we've seen it before
     * (caller should reject as a duplicate).
     */
    public boolean markIfFirstUse(String idempotencyKey) {
        String redisKey = KEY_PREFIX + idempotencyKey;
        Boolean wasSet = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, "true", TTL);
        return Boolean.TRUE.equals(wasSet);
    }
}