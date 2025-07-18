package com.moneymapper.budgettracker.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "security_audit_log", indexes = {
        @Index(columnList = "username"),
        @Index(columnList = "action"),
        @Index(columnList = "timestamp"),
        @Index(columnList = "ipAddress")
})
public class SecurityAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SecurityAction action;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "session_id")
    private String sessionId;

    private String details;

    @Enumerated(EnumType.STRING)
    private SecurityEventStatus status;

    // Constructors
    protected SecurityAuditLog() {}

    public SecurityAuditLog(String username, SecurityAction action, String ipAddress) {
        this.username = username;
        this.action = action;
        this.ipAddress = ipAddress;
        this.timestamp = LocalDateTime.now();
        this.status = SecurityEventStatus.SUCCESS;
    }

    public SecurityAuditLog(String username, SecurityAction action, String ipAddress, SecurityEventStatus status) {
        this.username = username;
        this.action = action;
        this.ipAddress = ipAddress;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    // Enums
    public enum SecurityAction {
        LOGIN_SUCCESS,
        LOGIN_FAILURE,
        LOGOUT,
        REGISTRATION,
        EMAIL_VERIFICATION,
        PASSWORD_RESET_REQUEST,
        PASSWORD_RESET_COMPLETE,
        PASSWORD_CHANGE,
        ACCOUNT_LOCKED,
        ACCOUNT_UNLOCKED,
        ROLE_CHANGED,
        TOKEN_REFRESH,
        SUSPICIOUS_ACTIVITY,
        BRUTE_FORCE_ATTEMPT,
        TWO_FACTOR_ENABLED,
        TWO_FACTOR_DISABLED,
        TWO_FACTOR_SUCCESS,
        TWO_FACTOR_FAILURE
    }

    public enum SecurityEventStatus {
        SUCCESS,
        FAILURE,
        WARNING,
        BLOCKED
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public SecurityAction getAction() {
        return action;
    }

    public void setAction(SecurityAction action) {
        this.action = action;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public SecurityEventStatus getStatus() {
        return status;
    }

    public void setStatus(SecurityEventStatus status) {
        this.status = status;
    }

    @PrePersist
    public void prePersist() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }
}