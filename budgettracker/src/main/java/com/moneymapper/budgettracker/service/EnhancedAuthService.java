package com.moneymapper.budgettracker.service;

import com.moneymapper.budgettracker.domain.*;
import com.moneymapper.budgettracker.dto.*;
import com.moneymapper.budgettracker.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnhancedAuthService {

    private final UserService userService;
    private final EmailService emailService;
    private final SecurityAuditService auditService;
    private final RateLimitingService rateLimitingService;
    private final TokenBlacklistService tokenBlacklistService;
    private final TwoFactorAuthService twoFactorService;

    private final EmailVerificationTokenRepository emailTokenRepository;
    private final PasswordResetTokenRepository passwordResetRepository;

    @Transactional
    public UserResponse registerWithEmailVerification(RegistrationRequest request, String ipAddress) {
        // Check rate limiting
        if (!rateLimitingService.isRegistrationAllowed(ipAddress)) {
            throw new IllegalStateException("Registration rate limit exceeded. Please try again later.");
        }

        // Register user but keep them unverified
        User user = userService.register(request);
        user.setEnabled(false); // User is disabled until email verification
        userService.updateUser(user);

        // Record registration attempt
        rateLimitingService.recordRegistrationAttempt(ipAddress);

        // Generate email verification token
        if (request.email() != null && !request.email().isEmpty()) {
            String token = generateSecureToken();
            EmailVerificationToken verificationToken = new EmailVerificationToken(user, token);
            emailTokenRepository.save(verificationToken);

            // Send verification email
            emailService.sendEmailVerification(request.email(), token, user.getUsername());
        }

        // Log the registration
        auditService.logSecurityEvent(user.getUsername(), SecurityAuditLog.SecurityAction.REGISTRATION,
                ipAddress, null);

        return userService.toUserResponse(user);
    }

    @Transactional
    public boolean verifyEmail(String token) {
        Optional<EmailVerificationToken> tokenOpt = emailTokenRepository.findByToken(token);

        if (tokenOpt.isEmpty()) {
            return false;
        }

        EmailVerificationToken verificationToken = tokenOpt.get();

        if (!verificationToken.isValid()) {
            return false;
        }

        // Enable the user account
        User user = verificationToken.getUser();
        user.setEnabled(true);
        userService.updateUser(user);

        // Mark token as used
        verificationToken.setUsed(true);
        emailTokenRepository.save(verificationToken);

        // Log the verification
        auditService.logSecurityEvent(user.getUsername(), SecurityAuditLog.SecurityAction.EMAIL_VERIFICATION,
                null, null);

        log.info("Email verified for user: {}", user.getUsername());
        return true;
    }

    @Transactional
    public void resendEmailVerification(String usernameOrEmail, String ipAddress) {
        // Check rate limiting
        if (!rateLimitingService.isEmailVerificationAllowed(usernameOrEmail)) {
            throw new IllegalStateException("Email verification rate limit exceeded. Please try again later.");
        }

        User user = userService.findByUsername(usernameOrEmail)
                .orElse(userService.findByEmail(usernameOrEmail).orElse(null));

        if (user == null || user.getEmail() == null) {
            // Don't reveal if user exists
            rateLimitingService.recordEmailVerificationAttempt(usernameOrEmail);
            return;
        }

        // Delete existing verification tokens
        emailTokenRepository.deleteByUser(user);

        // Generate new token
        String token = generateSecureToken();
        EmailVerificationToken verificationToken = new EmailVerificationToken(user, token);
        emailTokenRepository.save(verificationToken);

        // Send verification email
        emailService.sendEmailVerification(user.getEmail(), token, user.getUsername());

        // Record attempt
        rateLimitingService.recordEmailVerificationAttempt(usernameOrEmail);

        log.info("Email verification resent for user: {}", user.getUsername());
    }

    @Transactional
    public void requestPasswordReset(String usernameOrEmail, String ipAddress) {
        // Check rate limiting
        if (!rateLimitingService.isPasswordResetAllowed(usernameOrEmail)) {
            throw new IllegalStateException("Password reset rate limit exceeded. Please try again later.");
        }

        User user = userService.findByUsername(usernameOrEmail)
                .orElse(userService.findByEmail(usernameOrEmail).orElse(null));

        // Always record the attempt, even if user doesn't exist (to prevent enumeration)
        rateLimitingService.recordPasswordResetAttempt(usernameOrEmail);

        if (user == null || user.getEmail() == null) {
            // Don't reveal if user exists, but still log the attempt
            auditService.logSecurityEvent(usernameOrEmail, SecurityAuditLog.SecurityAction.PASSWORD_RESET_REQUEST,
                    ipAddress, null, SecurityAuditLog.SecurityEventStatus.FAILURE, "User not found");
            return;
        }

        // Delete existing reset tokens
        passwordResetRepository.deleteByUser(user);

        // Generate reset token
        String token = generateSecureToken();
        PasswordResetToken resetToken = new PasswordResetToken(user, token, 60); // 1 hour expiry
        passwordResetRepository.save(resetToken);

        // Send reset email
        emailService.sendPasswordReset(user.getEmail(), token, user.getUsername());

        // Log the request
        auditService.logSecurityEvent(user.getUsername(), SecurityAuditLog.SecurityAction.PASSWORD_RESET_REQUEST,
                ipAddress, null);

        log.info("Password reset requested for user: {}", user.getUsername());
    }

    @Transactional
    public boolean resetPassword(String token, String newPassword, String ipAddress) {
        Optional<PasswordResetToken> tokenOpt = passwordResetRepository.findByToken(token);

        if (tokenOpt.isEmpty()) {
            auditService.logSecurityEvent("unknown", SecurityAuditLog.SecurityAction.PASSWORD_RESET_COMPLETE,
                    ipAddress, null, SecurityAuditLog.SecurityEventStatus.FAILURE, "Invalid token");
            return false;
        }

        PasswordResetToken resetToken = tokenOpt.get();

        if (!resetToken.isValid()) {
            auditService.logSecurityEvent(resetToken.getUser().getUsername(),
                    SecurityAuditLog.SecurityAction.PASSWORD_RESET_COMPLETE,
                    ipAddress, null, SecurityAuditLog.SecurityEventStatus.FAILURE, "Expired token");
            return false;
        }

        User user = resetToken.getUser();

        // Update password
        user.setPassword(userService.getPasswordEncoder().encode(newPassword));
        userService.updateUser(user);

        // Mark token as used
        resetToken.setUsed(true);
        passwordResetRepository.save(resetToken);

        // Invalidate all existing JWT tokens for this user
        tokenBlacklistService.revokeTokensForPasswordChange(user.getUsername());

        // Send notification email
        if (user.getEmail() != null) {
            emailService.sendPasswordChangeNotification(user.getEmail(), user.getUsername());
        }

        // Log the successful reset
        auditService.logSecurityEvent(user.getUsername(), SecurityAuditLog.SecurityAction.PASSWORD_RESET_COMPLETE,
                ipAddress, null);

        log.info("Password reset completed for user: {}", user.getUsername());
        return true;
    }

    @Transactional
    public JwtResponse authenticateWithTwoFactor(LoginRequest loginRequest, String totpCode, String ipAddress, String userAgent) {
        // Check rate limiting first
        if (!rateLimitingService.isLoginAllowed(loginRequest.username())) {
            rateLimitingService.recordLoginAttempt(loginRequest.username());
            auditService.logSecurityEvent(loginRequest.username(), SecurityAuditLog.SecurityAction.LOGIN_FAILURE,
                    ipAddress, userAgent, SecurityAuditLog.SecurityEventStatus.BLOCKED, "Rate limited");

            Duration cooldown = rateLimitingService.getLoginCooldownTime(loginRequest.username());
            throw new IllegalStateException("Too many login attempts. Please try again in " +
                    cooldown.toMinutes() + " minutes.");
        }

        // Verify credentials
        Optional<User> userOpt = userService.findByUsername(loginRequest.username());
        if (userOpt.isEmpty() || !userService.getPasswordEncoder().matches(loginRequest.password(), userOpt.get().getPassword())) {
            rateLimitingService.recordLoginAttempt(loginRequest.username());
            auditService.logSecurityEvent(loginRequest.username(), SecurityAuditLog.SecurityAction.LOGIN_FAILURE,
                    ipAddress, userAgent, SecurityAuditLog.SecurityEventStatus.FAILURE, "Invalid credentials");
            throw new IllegalArgumentException("Invalid username or password");
        }

        User user = userOpt.get();

        // Check if account is enabled
        if (!user.isEnabled()) {
            auditService.logSecurityEvent(user.getUsername(), SecurityAuditLog.SecurityAction.LOGIN_FAILURE,
                    ipAddress, userAgent, SecurityAuditLog.SecurityEventStatus.FAILURE, "Account disabled");
            throw new IllegalStateException("Account is disabled. Please verify your email address.");
        }

        // Check two-factor authentication
        if (twoFactorService.isTwoFactorEnabled(user)) {
            if (totpCode == null || !twoFactorService.verifyTwoFactorCode(user, totpCode)) {
                rateLimitingService.recordLoginAttempt(loginRequest.username());
                auditService.logSecurityEvent(user.getUsername(), SecurityAuditLog.SecurityAction.TWO_FACTOR_FAILURE,
                        ipAddress, userAgent, SecurityAuditLog.SecurityEventStatus.FAILURE, "Invalid 2FA code");
                throw new IllegalArgumentException("Invalid two-factor authentication code");
            }

            auditService.logSecurityEvent(user.getUsername(), SecurityAuditLog.SecurityAction.TWO_FACTOR_SUCCESS,
                    ipAddress, userAgent);
        }

        // Check for suspicious activity
        if (auditService.isSuspiciousActivity(user.getUsername(), ipAddress)) {
            auditService.logSecurityEvent(user.getUsername(), SecurityAuditLog.SecurityAction.SUSPICIOUS_ACTIVITY,
                    ipAddress, userAgent, SecurityAuditLog.SecurityEventStatus.WARNING, "Suspicious login pattern");

            if (user.getEmail() != null) {
                emailService.sendSuspiciousActivityAlert(user.getEmail(), user.getUsername(), ipAddress, userAgent);
            }
        }

        // Clear failed login attempts on successful login
        rateLimitingService.clearLoginAttempts(loginRequest.username());

        // Generate JWT token
        String jwt = userService.getJwtUtil().generateToken(user);

        // Log successful login
        auditService.logSecurityEvent(user.getUsername(), SecurityAuditLog.SecurityAction.LOGIN_SUCCESS,
                ipAddress, userAgent);

        return new JwtResponse(jwt, user.getUsername(), user.getEmail(), user.getRoles());
    }

    @Transactional
    public void logout(String token, String username, String ipAddress, String userAgent) {
        // Blacklist the token
        tokenBlacklistService.blacklistToken(token, username, JwtTokenBlacklist.BlacklistReason.LOGOUT);

        // Log the logout
        auditService.logSecurityEvent(username, SecurityAuditLog.SecurityAction.LOGOUT, ipAddress, userAgent);

        log.info("User logged out: {}", username);
    }

    // Cleanup expired tokens (scheduled task)
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();

        emailTokenRepository.deleteExpiredTokens(now);
        passwordResetRepository.deleteExpiredTokens(now);

        log.info("Cleaned up expired verification and reset tokens");
    }

    private String generateSecureToken() {
        return UUID.randomUUID().toString().replace("-", "") +
                Long.toHexString(new SecureRandom().nextLong());
    }
}