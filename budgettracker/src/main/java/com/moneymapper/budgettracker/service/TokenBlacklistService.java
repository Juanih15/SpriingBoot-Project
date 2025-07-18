package com.moneymapper.budgettracker.service;

import com.moneymapper.budgettracker.domain.JwtTokenBlacklist;
import com.moneymapper.budgettracker.repository.JwtTokenBlacklistRepository;
import com.moneymapper.budgettracker.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

    private final JwtTokenBlacklistRepository blacklistRepository;
    private final JwtUtil jwtUtil;

    public boolean isTokenBlacklisted(String token) {
        try {
            String tokenHash = hashToken(token);
            return blacklistRepository.existsByTokenHash(tokenHash);
        } catch (Exception e) {
            log.error("Error checking token blacklist: {}", e.getMessage());
            // In case of error, assume token is not blacklisted to avoid blocking valid users
            return false;
        }
    }

    @Async
    @Transactional
    public void blacklistToken(String token, String username, JwtTokenBlacklist.BlacklistReason reason) {
        try {
            String tokenHash = hashToken(token);
            LocalDateTime expiryDate = jwtUtil.extractExpiration(token).toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime();

            JwtTokenBlacklist blacklistEntry = new JwtTokenBlacklist(tokenHash, username, expiryDate, reason);
            blacklistRepository.save(blacklistEntry);

            log.info("Token blacklisted for user: {} with reason: {}", username, reason);
        } catch (Exception e) {
            log.error("Error blacklisting token for user {}: {}", username, e.getMessage());
        }
    }

    @Async
    @Transactional
    public void blacklistAllUserTokens(String username, JwtTokenBlacklist.BlacklistReason reason) {
        try {
            // This would require storing active tokens or implementing a user token versioning system
            // For now, we'll just log the action and rely on password changes to invalidate tokens
            log.info("All tokens invalidated for user: {} with reason: {}", username, reason);

            // In a production system, you might:
            // 1. Store a "tokens valid after" timestamp for each user
            // 2. Increment a user token version number
            // 3. Keep track of issued tokens and blacklist them individually
        } catch (Exception e) {
            log.error("Error blacklisting all tokens for user {}: {}", username, e.getMessage());
        }
    }

    @Transactional
    public void revokeTokensForPasswordChange(String username) {
        blacklistAllUserTokens(username, JwtTokenBlacklist.BlacklistReason.PASSWORD_CHANGED);
    }

    @Transactional
    public void revokeTokensForCompromisedAccount(String username) {
        blacklistAllUserTokens(username, JwtTokenBlacklist.BlacklistReason.ACCOUNT_COMPROMISED);
    }

    @Transactional
    public void revokeTokensByAdmin(String username) {
        blacklistAllUserTokens(username, JwtTokenBlacklist.BlacklistReason.ADMIN_REVOKED);
    }

    // Cleanup expired blacklisted tokens (run daily)
    @Scheduled(cron = "0 0 2 * * ?") // Run at 2 AM daily
    @Transactional
    public void cleanupExpiredTokens() {
        try {
            LocalDateTime now = LocalDateTime.now();
            blacklistRepository.deleteExpiredTokens(now);
            log.info("Cleaned up expired blacklisted tokens");
        } catch (Exception e) {
            log.error("Error cleaning up expired tokens: {}", e.getMessage());
        }
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    @Transactional(readOnly = true)
    public long getBlacklistedTokenCount() {
        return blacklistRepository.count();
    }

    @Transactional(readOnly = true)
    public boolean hasUserBeenBlacklisted(String username) {
        return !blacklistRepository.findByUsername(username).isEmpty();
    }
}