package com.buildbetter.auth.service.ratelimit;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OtpRateLimiter {

    private final StringRedisTemplate redisTemplate;
    private final String PREFIX = "register_otp_limit:";

    public boolean canSendOtp(String userId) {

        String key = PREFIX + userId;

        String attempts = redisTemplate.opsForValue().get(key);

        return attempts == null || Integer.parseInt(attempts) < 5;
    }

    public void addOtpAttempt(String userId) {
        String key = PREFIX + userId;
        String attempts = redisTemplate.opsForValue().get(key);
        if (attempts == null) {
            redisTemplate.opsForValue().set(key, "1", 5, TimeUnit.MINUTES);
        } else {
            redisTemplate.opsForValue().increment(key);
        }
    }

    public void resetOtpAttempts(String userId) {
        redisTemplate.delete(PREFIX + userId);
    }
}
