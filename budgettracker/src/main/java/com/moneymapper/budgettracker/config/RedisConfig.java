package com.moneymapper.budgettracker.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j
public class RedisConfig {

    @Bean
    @ConditionalOnProperty(name = "spring.data.redis.host")
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use String serialization for both keys and values
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);

        template.afterPropertiesSet();

        log.info("Redis template configured for rate limiting");
        return template;
    }

    // Fallback configuration when Redis is not available The RateLimitingService will automatically fall back to in-memory storage
    @Bean
    @ConditionalOnProperty(name = "spring.data.redis.host", matchIfMissing = true)
    public RedisTemplate<String, String> inMemoryFallbackRedisTemplate() {
        log.warn("Redis not configured - rate limiting will use in-memory storage");
        return null; // RateLimitingService will handle null RedisTemplate gracefully
    }
}