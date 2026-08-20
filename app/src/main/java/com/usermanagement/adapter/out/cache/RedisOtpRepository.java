package com.usermanagement.adapter.out.cache;

import com.usermanagement.application.port.out.OtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RedisOtpRepository implements OtpRepository {

    private static final String OTP_KEY_PREFIX = "otp:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void saveOtp(String email, String otp, Duration ttl) {
        redisTemplate.opsForValue().set(OTP_KEY_PREFIX + email, otp, ttl);
    }

    @Override
    public Optional<String> findOtp(String email) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(OTP_KEY_PREFIX + email));
    }

    @Override
    public void deleteOtp(String email) {
        redisTemplate.delete(OTP_KEY_PREFIX + email);
    }
}
