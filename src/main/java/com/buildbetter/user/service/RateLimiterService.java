package com.buildbetter.user.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimiterService {
    private final StringRedisTemplate redisTemplate;

    public static final String PREFIX_OTP_RATE_LIMIT = "otp_rate_limit:";
    public static final String PREFIX_FORGOT_PASSWORD_RATE_LIMIT = "reset_password_rate_limit:";

    public boolean canSendOtp(String userId, String prefix) {
        String key = prefix + userId;

        String attempts = redisTemplate.opsForValue().get(key);

        return attempts == null || Integer.parseInt(attempts) < 5;
    }

    public void addOtpAttempt(String userId, String prefix) {
        String key = prefix + userId;
        String attempts = redisTemplate.opsForValue().get(key);
        if (attempts == null) {
            redisTemplate.opsForValue().set(key, "1");
        } else {
            redisTemplate.opsForValue().increment(key);
        }
    }

    public void resetOtpAttempts(String userId, String prefix) {
        redisTemplate.delete(prefix + userId);
    }
}