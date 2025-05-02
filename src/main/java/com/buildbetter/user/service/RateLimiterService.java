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
        log.info("Rate Limiter Service : canSendOtp");
        String key = prefix + userId;

        log.info("Rate Limiter Service : Checking if user is rate limited for key: " + key);
        String attempts = redisTemplate.opsForValue().get(key);

        return attempts == null || Integer.parseInt(attempts) < 5;
    }

    public void addOtpAttempt(String userId, String prefix) {
        log.info("Rate Limiter Service : addOtpAttempt");
        String key = prefix + userId;

        log.info("Rate Limiter Service : Get OTP attempt for key: " + key);
        String attempts = redisTemplate.opsForValue().get(key);
        if (attempts == null) {
            log.info("Rate Limiter Service : No attempts found, setting to 1");
            redisTemplate.opsForValue().set(key, "1");
        } else {
            log.info("Rate Limiter Service : Incrementing attempts for key: " + key);
            redisTemplate.opsForValue().increment(key);
        }
    }

    public void resetOtpAttempts(String userId, String prefix) {
        log.info("Rate Limiter Service : resetOtpAttempts");
        redisTemplate.delete(prefix + userId);
    }
}