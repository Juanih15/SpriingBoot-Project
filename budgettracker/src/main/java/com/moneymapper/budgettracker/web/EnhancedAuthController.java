package com.moneymapper.budgettracker.web;

import com.moneymapper.budgettracker.domain.User;
import com.moneymapper.budgettracker.dto.*;
import com.moneymapper.budgettracker.service.*;
import com.moneymapper.budgettracker.util.ClientIpUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class EnhancedAuthController {

    private final EnhancedAuthService enhancedAuthService;
    private final TwoFactorAuthService twoFactorService;
    private final SecurityAuditService auditService;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> registerUser(
            @Valid @RequestBody RegistrationRequest request,
            HttpServletRequest httpRequest) {
        try {
            String ipAddress = ClientIpUtils.getClientIpAddress(httpRequest);
            UserResponse userResponse = enhancedAuthService.registerWithEmailVerification(request, ipAddress);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("User registered successfully. Please check your email for verification.", userResponse));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<String>> verifyEmail(@RequestParam String token) {
        boolean verified = enhancedAuthService.verifyEmail(token);

        if (verified) {
            return ResponseEntity.ok(ApiResponse.success("Email verified successfully. You can now log in."));
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid or expired verification token"));
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<String>> resendVerification(
            @RequestParam String usernameOrEmail,
            HttpServletRequest request) {
        try {
            String ipAddress = ClientIpUtils.getClientIpAddress(request);
            enhancedAuthService.resendEmailVerification(usernameOrEmail, ipAddress);
            return ResponseEntity.ok(ApiResponse.success("Verification email sent if account exists"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            @RequestParam(required = false) String totpCode,
            HttpServletRequest request) {
        try {
            String ipAddress = ClientIpUtils.getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");

            JwtResponse jwtResponse = enhancedAuthService.authenticateWithTwoFactor(
                    loginRequest, totpCode, ipAddress, userAgent);

            return ResponseEntity.ok(ApiResponse.success("Login successful", jwtResponse));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            @RequestHeader("Authorization") String authHeader,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {

        if (authHeader != null && authHeader.startsWith("Bearer ") && user != null) {
            String token = authHeader.substring(7);
            String ipAddress = ClientIpUtils.getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");

            enhancedAuthService.logout(token, user.getUsername(), ipAddress, userAgent);
        }

        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(
            @RequestParam String usernameOrEmail,
            HttpServletRequest request) {
        try {
            String ipAddress = ClientIpUtils.getClientIpAddress(request);
            enhancedAuthService.requestPasswordReset(usernameOrEmail, ipAddress);
            return ResponseEntity.ok(ApiResponse.success("Password reset email sent if account exists"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword,
            HttpServletRequest request) {

        String ipAddress = ClientIpUtils.getClientIpAddress(request);
        boolean reset = enhancedAuthService.resetPassword(token, newPassword, ipAddress);

        if (reset) {
            return ResponseEntity.ok(ApiResponse.success("Password reset successfully"));
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid or expired reset token"));
        }
    }

    // Two-Factor Authentication Endpoints

    @PostMapping("/2fa/setup")
    public ResponseEntity<ApiResponse<TwoFactorSetupResponse>> setupTwoFactor(@AuthenticationPrincipal User user) {
        String secret = twoFactorService.generateSecretKey(user);
        String qrCodeUrl = twoFactorService.generateQRCodeURL(user, secret, "MoneyMapper");

        TwoFactorSetupResponse response = new TwoFactorSetupResponse(secret, qrCodeUrl);
        return ResponseEntity.ok(ApiResponse.success("Two-factor authentication setup initiated", response));
    }

    @PostMapping("/2fa/enable")
    public ResponseEntity<ApiResponse<String>> enableTwoFactor(
            @AuthenticationPrincipal User user,
            @RequestParam String totpCode) {

        boolean enabled = twoFactorService.enableTwoFactor(user, totpCode);

        if (enabled) {
            return ResponseEntity.ok(ApiResponse.success("Two-factor authentication enabled successfully"));
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid TOTP code"));
        }
    }

    @PostMapping("/2fa/disable")
    public ResponseEntity<ApiResponse<String>> disableTwoFactor(@AuthenticationPrincipal User user) {
        twoFactorService.disableTwoFactor(user);
        return ResponseEntity.ok(ApiResponse.success("Two-factor authentication disabled"));
    }

    @GetMapping("/2fa/backup-codes")
    public ResponseEntity<ApiResponse<List<String>>> getBackupCodes(@AuthenticationPrincipal User user) {
        List<String> backupCodes = twoFactorService.getBackupCodes(user);
        return ResponseEntity.ok(ApiResponse.success(backupCodes));
    }

    @PostMapping("/2fa/regenerate-backup-codes")
    public ResponseEntity<ApiResponse<List<String>>> regenerateBackupCodes(@AuthenticationPrincipal User user) {
        List<String> newBackupCodes = twoFactorService.regenerateBackupCodes(user);
        return ResponseEntity.ok(ApiResponse.success("Backup codes regenerated", newBackupCodes));
    }

    @GetMapping("/2fa/status")
    public ResponseEntity<ApiResponse<TwoFactorStatusResponse>> getTwoFactorStatus(@AuthenticationPrincipal User user) {
        boolean enabled = twoFactorService.isTwoFactorEnabled(user);
        TwoFactorStatusResponse response = new TwoFactorStatusResponse(enabled);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Security Information Endpoints

    @GetMapping("/security/history")
    public ResponseEntity<ApiResponse<List<SecurityEventResponse>>> getSecurityHistory(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "50") int limit) {

        var events = auditService.getUserSecurityHistory(user.getUsername(), limit);
        var responses = events.stream()
                .map(event -> new SecurityEventResponse(
                        event.getAction().toString(),
                        event.getTimestamp(),
                        event.getIpAddress(),
                        event.getStatus().toString(),
                        event.getDetails()
                ))
                .toList();

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("User not authenticated"));
        }

        UserResponse userResponse = userService.toUserResponse(user);
        return ResponseEntity.ok(ApiResponse.success(userResponse));
    }
}

// Additional DTOs for enhanced authentication

record TwoFactorSetupResponse(String secret, String qrCodeUrl) {}

record TwoFactorStatusResponse(boolean enabled) {}

record SecurityEventResponse(
        String action,
        java.time.LocalDateTime timestamp,
        String ipAddress,
        String status,
        String details
) {}