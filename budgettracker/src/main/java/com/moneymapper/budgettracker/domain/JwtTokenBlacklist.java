package com.moneymapper.budgettracker.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "jwt_token_blacklist", indexes = {
        @Index(columnList = "tokenHash", unique = true),
        @Index(columnList = "expiryDate")
})
public class JwtTokenBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, name = "token_hash")
    private String tokenHash; // Store hash of token, not the actual token

    @Column(nullable = false)
    private String username;

    @Column(nullable = false, name = "blacklisted_at")
    private LocalDateTime blacklistedAt;

    @Column(nullable = false, name = "expiry_date")
    private LocalDateTime expiryDate;

    @Enumerated(EnumType.STRING)
    private BlacklistReason reason;

    // Constructors
    protected JwtTokenBlacklist() {}

    public JwtTokenBlacklist(String tokenHash, String username, LocalDateTime expiryDate, BlacklistReason reason) {
        this.tokenHash = tokenHash;
        this.username = username;
        this.expiryDate = expiryDate;
        this.reason = reason;
        this.blacklistedAt = LocalDateTime.now();
    }

    // Enum for blacklist reasons
    public enum BlacklistReason {
        LOGOUT,
        PASSWORD_CHANGED,
        ACCOUNT_COMPROMISED,
        ADMIN_REVOKED,
        SUSPICIOUS_ACTIVITY,
        TOKEN_ROTATION
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getBlacklistedAt() {
        return blacklistedAt;
    }

    public void setBlacklistedAt(LocalDateTime blacklistedAt) {
        this.blacklistedAt = blacklistedAt;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public BlacklistReason getReason() {
        return reason;
    }

    public void setReason(BlacklistReason reason) {
        this.reason = reason;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    @PrePersist
    public void prePersist() {
        if (this.blacklistedAt == null) {
            this.blacklistedAt = LocalDateTime.now();
        }
    }
}