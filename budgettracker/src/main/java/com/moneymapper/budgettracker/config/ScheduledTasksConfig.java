package com.moneymapper.budgettracker.config;

import com.moneymapper.budgettracker.service.EnhancedAuthService;
import com.moneymapper.budgettracker.service.SecurityAuditService;
import com.moneymapper.budgettracker.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasksConfig {

    private final EnhancedAuthService enhancedAuthService;
    private final SecurityAuditService securityAuditService;
    private final TokenBlacklistService tokenBlacklistService;

    // Clean up expired email verification and password reset tokens Runs every day at 2:00 AM

    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredTokens() {
        log.info("Starting cleanup of expired tokens...");
        try {
            enhancedAuthService.cleanupExpiredTokens();
            log.info("Completed cleanup of expired tokens");
        } catch (Exception e) {
            log.error("Error during token cleanup: {}", e.getMessage(), e);
        }
    }

    //Clean up old security audit logs Runs every Sunday at 3:00 AM
    @Scheduled(cron = "0 0 3 * * SUN")
    @ConditionalOnProperty(name = "app.security.audit.cleanup-enabled", havingValue = "true", matchIfMissing = true)
    public void cleanupOldAuditLogs() {
        log.info("Starting cleanup of old audit logs...");
        try {
            // Keep logs for 90 days by default
            securityAuditService.cleanupOldAuditLogs(90);
            log.info("Completed cleanup of old audit logs");
        } catch (Exception e) {
            log.error("Error during audit log cleanup: {}", e.getMessage(), e);
        }
    }

    // Clean up expired blacklisted tokens Runs every day at 1:00 AM
    @Scheduled(cron = "0 0 1 * * ?")
    @ConditionalOnProperty(name = "app.security.token-blacklist.cleanup-enabled", havingValue = "true", matchIfMissing = true)
    public void cleanupExpiredBlacklistedTokens() {
        log.info("Starting cleanup of expired blacklisted tokens...");
        try {
            tokenBlacklistService.cleanupExpiredTokens();
            log.info("Completed cleanup of expired blacklisted tokens");
        } catch (Exception e) {
            log.error("Error during blacklisted token cleanup: {}", e.getMessage(), e);
        }
    }

    // Security health check - Monitor for suspicious patterns Runs every hour
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    public void securityHealthCheck() {
        try {
            long blacklistedTokens = tokenBlacklistService.getBlacklistedTokenCount();
            log.debug("Security health check - Blacklisted tokens: {}", blacklistedTokens);

            //  add more sophisticated:
            // - Check for unusual login patterns
            // - Monitor failed authentication rates
            // - Alert on suspicious IP addresses
            // - Track rate limiting violations

        } catch (Exception e) {
            log.error("Error during security health check: {}", e.getMessage(), e);
        }
    }
}