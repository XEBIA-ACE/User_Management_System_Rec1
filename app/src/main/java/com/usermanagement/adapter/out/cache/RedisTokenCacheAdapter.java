package com.usermanagement.adapter.out.cache;

import com.usermanagement.domain.port.out.TokenCachePort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
public class RedisTokenCacheAdapter implements TokenCachePort {

    private final StringRedisTemplate redisTemplate;

    public RedisTokenCacheAdapter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void store(String key, String value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    @Override
    public Optional<String> retrieve(String key) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key));
    }

    @Override
    public void invalidate(String key) {
        redisTemplate.delete(key);
    }
}
