package com.moneymapper.budgettracker.service;

import com.moneymapper.budgettracker.domain.SecurityAuditLog;
import com.moneymapper.budgettracker.repository.SecurityAuditLogRepository;
import com.moneymapper.budgettracker.util.ClientIpUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityAuditService {

    private final SecurityAuditLogRepository auditLogRepository;

    @Async
    @Transactional
    public void logSecurityEvent(String username, SecurityAuditLog.SecurityAction action,
                                 String ipAddress, String userAgent) {
        logSecurityEvent(username, action, ipAddress, userAgent, SecurityAuditLog.SecurityEventStatus.SUCCESS, null);
    }

    @Async
    @Transactional
    public void logSecurityEvent(String username, SecurityAuditLog.SecurityAction action,
                                 String ipAddress, String userAgent,
                                 SecurityAuditLog.SecurityEventStatus status, String details) {
        try {
            SecurityAuditLog auditLog = new SecurityAuditLog(username, action, ipAddress, status);
            auditLog.setUserAgent(userAgent);
            auditLog.setDetails(details);

            auditLogRepository.save(auditLog);

            // Log to application logs as well for immediate monitoring
            log.info("Security Event: {} - {} - {} - {} - {}",
                    username, action, status, ipAddress, details);

        } catch (Exception e) {
            log.error("Failed to log security event: {}", e.getMessage(), e);
        }
    }

    @Async
    @Transactional
    public void logSecurityEventFromRequest(String username, SecurityAuditLog.SecurityAction action,
                                            HttpServletRequest request) {
        String ipAddress = ClientIpUtils.getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        logSecurityEvent(username, action, ipAddress, userAgent);
    }

    @Async
    @Transactional
    public void logSecurityEventFromRequest(String username, SecurityAuditLog.SecurityAction action,
                                            HttpServletRequest request, SecurityAuditLog.SecurityEventStatus status,
                                            String details) {
        String ipAddress = ClientIpUtils.getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        logSecurityEvent(username, action, ipAddress, userAgent, status, details);
    }

    @Transactional(readOnly = true)
    public List<SecurityAuditLog> getUserSecurityHistory(String username, int limit) {
        return auditLogRepository.findByUsernameOrderByTimestampDesc(username)
                .stream()
                .limit(limit)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SecurityAuditLog> getRecentSecurityEvents(String username, int hoursBack) {
        LocalDateTime since = LocalDateTime.now().minusHours(hoursBack);
        return auditLogRepository.findRecentByUsername(username, since);
    }

    @Transactional(readOnly = true)
    public long getFailedLoginAttempts(String username, int minutesBack) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(minutesBack);
        return auditLogRepository.countFailedAttempts(username,
                SecurityAuditLog.SecurityAction.LOGIN_FAILURE,
                SecurityAuditLog.SecurityEventStatus.FAILURE,
                since);
    }

    @Transactional(readOnly = true)
    public boolean isSuspiciousActivity(String username, String ipAddress) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);

        // Check for multiple failed login attempts
        long failedAttempts = getFailedLoginAttempts(username, 60);
        if (failedAttempts >= 5) {
            return true;
        }

        // Check for rapid login attempts from different IPs
        List<SecurityAuditLog> recentEvents = getRecentSecurityEvents(username, 1);
        long uniqueIps = recentEvents.stream()
                .filter(event -> event.getAction() == SecurityAuditLog.SecurityAction.LOGIN_SUCCESS ||
                        event.getAction() == SecurityAuditLog.SecurityAction.LOGIN_FAILURE)
                .map(SecurityAuditLog::getIpAddress)
                .distinct()
                .count();

        return uniqueIps >= 3; // 3 or more different IPs in 1 hour
    }

    @Transactional
    public void cleanupOldAuditLogs(int daysToKeep) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(daysToKeep);
        auditLogRepository.deleteOldLogs(cutoff);
        log.info("Cleaned up audit logs older than {} days", daysToKeep);
    }
}