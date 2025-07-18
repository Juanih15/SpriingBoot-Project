package com.moneymapper.budgettracker.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig {

    @Bean(name = "emailTaskExecutor")
    public Executor emailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("email-");
        executor.setRejectedExecutionHandler((runnable, executor1) -> {
            log.warn("Email task rejected, queue full. Task: {}", runnable.toString());
        });
        executor.initialize();
        return executor;
    }

    @Bean(name = "auditTaskExecutor")
    public Executor auditTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("audit-");
        executor.setRejectedExecutionHandler((runnable, executor1) -> {
            log.error("Audit task rejected, queue full. This may result in lost audit logs: {}", runnable.toString());
        });
        executor.initialize();
        return executor;
    }

    @Bean(name = "securityTaskExecutor")
    public Executor securityTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("security-");
        executor.setRejectedExecutionHandler((runnable, executor1) -> {
            log.warn("Security task rejected, queue full. Task: {}", runnable.toString());
        });
        executor.initialize();
        return executor;
    }
}