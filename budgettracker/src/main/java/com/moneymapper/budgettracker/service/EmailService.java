package com.moneymapper.budgettracker.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@moneymapper.com}")
    private String fromEmail;

    @Value("${app.mail.base-url:http://localhost:3000}")
    private String baseUrl;

    @Async
    public void sendEmailVerification(String to, String token, String username) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Verify Your MoneyMapper Account");

        String verificationUrl = baseUrl + "/verify-email?token=" + token;

        String emailBody = String.format(
                "Hello %s,\n\n" +
                        "Thank you for registering with MoneyMapper!\n\n" +
                        "Please click the link below to verify your email address:\n" +
                        "%s\n\n" +
                        "This link will expire in 24 hours.\n\n" +
                        "If you didn't create this account, please ignore this email.\n\n" +
                        "Best regards,\n" +
                        "The MoneyMapper Team",
                username, verificationUrl
        );

        message.setText(emailBody);
        mailSender.send(message);
    }

    @Async
    public void sendPasswordReset(String to, String token, String username) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Reset Your MoneyMapper Password");

        String resetUrl = baseUrl + "/reset-password?token=" + token;

        String emailBody = String.format(
                "Hello %s,\n\n" +
                        "You requested to reset your password for your MoneyMapper account.\n\n" +
                        "Please click the link below to reset your password:\n" +
                        "%s\n\n" +
                        "This link will expire in 1 hour for security reasons.\n\n" +
                        "If you didn't request this password reset, please ignore this email and your password will remain unchanged.\n\n" +
                        "Best regards,\n" +
                        "The MoneyMapper Team",
                username, resetUrl
        );

        message.setText(emailBody);
        mailSender.send(message);
    }

    @Async
    public void sendPasswordChangeNotification(String to, String username) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("MoneyMapper Password Changed Successfully");

        String emailBody = String.format(
                "Hello %s,\n\n" +
                        "Your MoneyMapper account password has been successfully changed.\n\n" +
                        "If you didn't make this change, please contact our support team immediately.\n\n" +
                        "Best regards,\n" +
                        "The MoneyMapper Team",
                username
        );

        message.setText(emailBody);
        mailSender.send(message);
    }

    @Async
    public void sendSuspiciousActivityAlert(String to, String username, String ipAddress, String userAgent) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("MoneyMapper Security Alert - Suspicious Activity");

        String emailBody = String.format(
                "Hello %s,\n\n" +
                        "We detected suspicious activity on your MoneyMapper account.\n\n" +
                        "Details:\n" +
                        "- IP Address: %s\n" +
                        "- User Agent: %s\n" +
                        "- Time: %s\n\n" +
                        "If this was you, you can ignore this email.\n" +
                        "If this wasn't you, please change your password immediately and contact support.\n\n" +
                        "Best regards,\n" +
                        "The MoneyMapper Security Team",
                username, ipAddress, userAgent, java.time.LocalDateTime.now()
        );

        message.setText(emailBody);
        mailSender.send(message);
    }

    @Async
    public void sendTwoFactorSetupNotification(String to, String username) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("MoneyMapper Two-Factor Authentication Enabled");

        String emailBody = String.format(
                "Hello %s,\n\n" +
                        "Two-factor authentication has been successfully enabled on your MoneyMapper account.\n\n" +
                        "Your account is now more secure. You'll need your authenticator app to sign in.\n\n" +
                        "If you didn't enable this feature, please contact our support team immediately.\n\n" +
                        "Best regards,\n" +
                        "The MoneyMapper Security Team",
                username
        );

        message.setText(emailBody);
        mailSender.send(message);
    }
}