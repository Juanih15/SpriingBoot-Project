package com.moneymapper.budgettracker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@Slf4j
public class RateLimitingService {

    private final RedisTemplate<String, String> redisTemplate;

    // Fallback to in-memory if Redis is not available
    private final ConcurrentMap<String, RateLimitBucket> inMemoryBuckets = new ConcurrentHashMap<>();

    // Rate limit configurations
    private static final int LOGIN_ATTEMPTS_LIMIT = 5;
    private static final int LOGIN_WINDOW_MINUTES = 15;

    private static final int REGISTRATION_LIMIT = 3;
    private static final int REGISTRATION_WINDOW_HOURS = 1;

    private static final int PASSWORD_RESET_LIMIT = 3;
    private static final int PASSWORD_RESET_WINDOW_HOURS = 1;

    private static final int EMAIL_VERIFICATION_LIMIT = 5;
    private static final int EMAIL_VERIFICATION_WINDOW_HOURS = 1;

    // Constructor with @Qualifier to specify which Redis template to use
    public RateLimitingService(@Qualifier("stringRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isLoginAllowed(String identifier) {
        return isActionAllowed("login:" + identifier, LOGIN_ATTEMPTS_LIMIT, Duration.ofMinutes(LOGIN_WINDOW_MINUTES));
    }

    public void recordLoginAttempt(String identifier) {
        recordAttempt("login:" + identifier, Duration.ofMinutes(LOGIN_WINDOW_MINUTES));
    }

    public boolean isRegistrationAllowed(String ipAddress) {
        return isActionAllowed("register:" + ipAddress, REGISTRATION_LIMIT, Duration.ofHours(REGISTRATION_WINDOW_HOURS));
    }

    public void recordRegistrationAttempt(String ipAddress) {
        recordAttempt("register:" + ipAddress, Duration.ofHours(REGISTRATION_WINDOW_HOURS));
    }

    public boolean isPasswordResetAllowed(String identifier) {
        return isActionAllowed("password_reset:" + identifier, PASSWORD_RESET_LIMIT, Duration.ofHours(PASSWORD_RESET_WINDOW_HOURS));
    }

    public void recordPasswordResetAttempt(String identifier) {
        recordAttempt("password_reset:" + identifier, Duration.ofHours(PASSWORD_RESET_WINDOW_HOURS));
    }

    public boolean isEmailVerificationAllowed(String identifier) {
        return isActionAllowed("email_verify:" + identifier, EMAIL_VERIFICATION_LIMIT, Duration.ofHours(EMAIL_VERIFICATION_WINDOW_HOURS));
    }

    public void recordEmailVerificationAttempt(String identifier) {
        recordAttempt("email_verify:" + identifier, Duration.ofHours(EMAIL_VERIFICATION_WINDOW_HOURS));
    }

    public void clearLoginAttempts(String identifier) {
        clearAttempts("login:" + identifier);
    }

    public int getRemainingLoginAttempts(String identifier) {
        return getRemainingAttempts("login:" + identifier, LOGIN_ATTEMPTS_LIMIT);
    }

    public Duration getLoginCooldownTime(String identifier) {
        return getCooldownTime("login:" + identifier);
    }

    private boolean isActionAllowed(String key, int limit, Duration window) {
        try {
            if (redisTemplate != null && redisTemplate.getConnectionFactory() != null) {
                return isActionAllowedRedis(key, limit, window);
            } else {
                return isActionAllowedInMemory(key, limit, window);
            }
        } catch (Exception e) {
            log.warn("Redis unavailable, falling back to in-memory rate limiting: {}", e.getMessage());
            return isActionAllowedInMemory(key, limit, window);
        }
    }

    private boolean isActionAllowedRedis(String key, int limit, Duration window) {
        String count = redisTemplate.opsForValue().get(key);
        int currentCount = count != null ? Integer.parseInt(count) : 0;
        return currentCount < limit;
    }

    private boolean isActionAllowedInMemory(String key, int limit, Duration window) {
        RateLimitBucket bucket = inMemoryBuckets.computeIfAbsent(key, k -> new RateLimitBucket());
        return bucket.isAllowed(limit, window);
    }

    private void recordAttempt(String key, Duration window) {
        try {
            if (redisTemplate != null && redisTemplate.getConnectionFactory() != null) {
                recordAttemptRedis(key, window);
            } else {
                recordAttemptInMemory(key, window);
            }
        } catch (Exception e) {
            log.warn("Redis unavailable, falling back to in-memory rate limiting: {}", e.getMessage());
            recordAttemptInMemory(key, window);
        }
    }

    private void recordAttemptRedis(String key, Duration window) {
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, window);
    }

    private void recordAttemptInMemory(String key, Duration window) {
        RateLimitBucket bucket = inMemoryBuckets.computeIfAbsent(key, k -> new RateLimitBucket());
        bucket.recordAttempt();
    }

    private void clearAttempts(String key) {
        try {
            if (redisTemplate != null && redisTemplate.getConnectionFactory() != null) {
                redisTemplate.delete(key);
            } else {
                inMemoryBuckets.remove(key);
            }
        } catch (Exception e) {
            log.warn("Redis unavailable: {}", e.getMessage());
            inMemoryBuckets.remove(key);
        }
    }

    private int getRemainingAttempts(String key, int limit) {
        try {
            if (redisTemplate != null && redisTemplate.getConnectionFactory() != null) {
                String count = redisTemplate.opsForValue().get(key);
                int currentCount = count != null ? Integer.parseInt(count) : 0;
                return Math.max(0, limit - currentCount);
            } else {
                RateLimitBucket bucket = inMemoryBuckets.get(key);
                return bucket != null ? Math.max(0, limit - bucket.getCount()) : limit;
            }
        } catch (Exception e) {
            log.warn("Redis unavailable: {}", e.getMessage());
            RateLimitBucket bucket = inMemoryBuckets.get(key);
            return bucket != null ? Math.max(0, limit - bucket.getCount()) : limit;
        }
    }

    private Duration getCooldownTime(String key) {
        try {
            if (redisTemplate != null && redisTemplate.getConnectionFactory() != null) {
                Long ttl = redisTemplate.getExpire(key);
                return ttl != null && ttl > 0 ? Duration.ofSeconds(ttl) : Duration.ZERO;
            } else {
                RateLimitBucket bucket = inMemoryBuckets.get(key);
                return bucket != null ? bucket.getRemainingCooldown() : Duration.ZERO;
            }
        } catch (Exception e) {
            log.warn("Redis unavailable: {}", e.getMessage());
            RateLimitBucket bucket = inMemoryBuckets.get(key);
            return bucket != null ? bucket.getRemainingCooldown() : Duration.ZERO;
        }
    }

    // In-memory rate limit bucket implementation
    private static class RateLimitBucket {
        private int count = 0;
        private long windowStart = System.currentTimeMillis();
        private Duration windowDuration = Duration.ofMinutes(15);

        public synchronized boolean isAllowed(int limit, Duration window) {
            this.windowDuration = window;
            cleanupIfWindowExpired();
            return count < limit;
        }

        public synchronized void recordAttempt() {
            cleanupIfWindowExpired();
            count++;
        }

        public synchronized int getCount() {
            cleanupIfWindowExpired();
            return count;
        }

        public synchronized Duration getRemainingCooldown() {
            long elapsed = System.currentTimeMillis() - windowStart;
            long remaining = windowDuration.toMillis() - elapsed;
            return remaining > 0 ? Duration.ofMillis(remaining) : Duration.ZERO;
        }

        private void cleanupIfWindowExpired() {
            long elapsed = System.currentTimeMillis() - windowStart;
            if (elapsed >= windowDuration.toMillis()) {
                count = 0;
                windowStart = System.currentTimeMillis();
            }
        }
    }
}