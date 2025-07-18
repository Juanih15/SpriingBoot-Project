package com.moneymapper.budgettracker.service;

import com.moneymapper.budgettracker.domain.TwoFactorAuth;
import com.moneymapper.budgettracker.domain.User;
import com.moneymapper.budgettracker.repository.TwoFactorAuthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TwoFactorAuthService {

    private final TwoFactorAuthRepository twoFactorRepository;
    private final EmailService emailService;

    private static final String ALGORITHM = "HmacSHA1";
    private static final int CODE_LENGTH = 6;
    private static final int TIME_STEP = 30; // 30 seconds
    private static final int WINDOW = 1; // Allow 1 time step before/after current

    @Transactional
    public String generateSecretKey(User user) {
        // Generate a 32-character base32 secret
        byte[] bytes = new byte[20];
        new SecureRandom().nextBytes(bytes);
        String secret = Base32.encode(bytes);

        TwoFactorAuth twoFactor = twoFactorRepository.findByUser(user)
                .orElse(new TwoFactorAuth(user, secret));
        twoFactor.setSecretKey(secret);
        twoFactor.setSetupCompleted(false);

        twoFactorRepository.save(twoFactor);

        return secret;
    }

    @Transactional
    public boolean enableTwoFactor(User user, String totpCode) {
        Optional<TwoFactorAuth> twoFactorOpt = twoFactorRepository.findByUser(user);
        if (twoFactorOpt.isEmpty()) {
            return false;
        }

        TwoFactorAuth twoFactor = twoFactorOpt.get();

        // Verify the TOTP code before enabling
        if (!verifyTOTP(twoFactor.getSecretKey(), totpCode)) {
            return false;
        }

        twoFactor.setEnabled(true);
        twoFactor.setSetupCompleted(true);
        twoFactor.setBackupCodes(generateBackupCodes());
        twoFactorRepository.save(twoFactor);

        // Send notification email
        if (user.getEmail() != null) {
            emailService.sendTwoFactorSetupNotification(user.getEmail(), user.getUsername());
        }

        log.info("Two-factor authentication enabled for user: {}", user.getUsername());
        return true;
    }

    @Transactional
    public void disableTwoFactor(User user) {
        twoFactorRepository.findByUser(user).ifPresent(twoFactor -> {
            twoFactor.setEnabled(false);
            twoFactorRepository.save(twoFactor);
            log.info("Two-factor authentication disabled for user: {}", user.getUsername());
        });
    }

    @Transactional(readOnly = true)
    public boolean isTwoFactorEnabled(User user) {
        return twoFactorRepository.findByUser(user)
                .map(TwoFactorAuth::isEnabled)
                .orElse(false);
    }

    @Transactional
    public boolean verifyTwoFactorCode(User user, String code) {
        Optional<TwoFactorAuth> twoFactorOpt = twoFactorRepository.findByUser(user);
        if (twoFactorOpt.isEmpty() || !twoFactorOpt.get().isEnabled()) {
            return false;
        }

        TwoFactorAuth twoFactor = twoFactorOpt.get();

        // First try TOTP code
        if (verifyTOTP(twoFactor.getSecretKey(), code)) {
            twoFactor.setLastUsed(java.time.LocalDateTime.now());
            twoFactorRepository.save(twoFactor);
            return true;
        }

        // Then try backup codes
        if (verifyBackupCode(twoFactor, code)) {
            twoFactor.setLastUsed(java.time.LocalDateTime.now());
            twoFactorRepository.save(twoFactor);
            return true;
        }

        return false;
    }

    public String generateQRCodeURL(User user, String secret, String issuer) {
        String accountName = user.getEmail() != null ? user.getEmail() : user.getUsername();
        return String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=%d&period=%d",
                issuer, accountName, secret, issuer, CODE_LENGTH, TIME_STEP
        );
    }

    @Transactional(readOnly = true)
    public List<String> getBackupCodes(User user) {
        return twoFactorRepository.findByUser(user)
                .map(twoFactor -> parseBackupCodes(twoFactor.getBackupCodes()))
                .orElse(Collections.emptyList());
    }

    @Transactional
    public List<String> regenerateBackupCodes(User user) {
        Optional<TwoFactorAuth> twoFactorOpt = twoFactorRepository.findByUser(user);
        if (twoFactorOpt.isEmpty()) {
            return Collections.emptyList();
        }

        TwoFactorAuth twoFactor = twoFactorOpt.get();
        String newBackupCodes = generateBackupCodes();
        twoFactor.setBackupCodes(newBackupCodes);
        twoFactorRepository.save(twoFactor);

        return parseBackupCodes(newBackupCodes);
    }

    private boolean verifyTOTP(String secret, String code) {
        try {
            long currentTime = Instant.now().getEpochSecond() / TIME_STEP;

            // Check current time and +/- 1 time window for clock skew
            for (int i = -WINDOW; i <= WINDOW; i++) {
                String generatedCode = generateTOTP(secret, currentTime + i);
                if (generatedCode.equals(code)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.error("Error verifying TOTP code: {}", e.getMessage());
            return false;
        }
    }

    private String generateTOTP(String secret, long timeCounter)
            throws NoSuchAlgorithmException, InvalidKeyException {

        byte[] key = Base32.decode(secret);
        byte[] data = ByteBuffer.allocate(8).putLong(timeCounter).array();

        Mac mac = Mac.getInstance(ALGORITHM);
        mac.init(new SecretKeySpec(key, ALGORITHM));
        byte[] hash = mac.doFinal(data);

        int offset = hash[hash.length - 1] & 0x0F;
        int binary = ((hash[offset] & 0x7F) << 24) |
                ((hash[offset + 1] & 0xFF) << 16) |
                ((hash[offset + 2] & 0xFF) << 8) |
                (hash[offset + 3] & 0xFF);

        int otp = binary % (int) Math.pow(10, CODE_LENGTH);
        return String.format("%0" + CODE_LENGTH + "d", otp);
    }

    private String generateBackupCodes() {
        List<String> codes = new ArrayList<>();
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < 10; i++) {
            String code = String.format("%08d", random.nextInt(100000000));
            codes.add(code);
        }

        // Store as JSON array
        return "[\"" + String.join("\",\"", codes) + "\"]";
    }

    private List<String> parseBackupCodes(String backupCodesJson) {
        if (backupCodesJson == null || backupCodesJson.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            // Simple JSON parsing for backup codes
            String cleaned = backupCodesJson.replaceAll("[\\[\\]\"]", "");
            return Arrays.asList(cleaned.split(","));
        } catch (Exception e) {
            log.error("Error parsing backup codes: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private boolean verifyBackupCode(TwoFactorAuth twoFactor, String code) {
        List<String> backupCodes = parseBackupCodes(twoFactor.getBackupCodes());
        if (backupCodes.contains(code)) {
            // Remove used backup code
            backupCodes.remove(code);
            String updatedCodes = "[\"" + String.join("\",\"", backupCodes) + "\"]";
            twoFactor.setBackupCodes(updatedCodes);
            return true;
        }
        return false;
    }

    // Simple Base32 encoding/decoding
    private static class Base32 {
        private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        private static final char[] CHARS = ALPHABET.toCharArray();

        public static String encode(byte[] data) {
            StringBuilder result = new StringBuilder();
            int buffer = 0;
            int bufferLength = 0;

            for (byte b : data) {
                buffer = (buffer << 8) | (b & 0xFF);
                bufferLength += 8;

                while (bufferLength >= 5) {
                    int index = (buffer >> (bufferLength - 5)) & 0x1F;
                    result.append(CHARS[index]);
                    bufferLength -= 5;
                }
            }

            if (bufferLength > 0) {
                int index = (buffer << (5 - bufferLength)) & 0x1F;
                result.append(CHARS[index]);
            }

            return result.toString();
        }

        public static byte[] decode(String encoded) {
            encoded = encoded.toUpperCase().replaceAll("[^A-Z2-7]", "");

            ByteBuffer buffer = ByteBuffer.allocate(encoded.length() * 5 / 8);
            int acc = 0;
            int bits = 0;

            for (char c : encoded.toCharArray()) {
                int value = ALPHABET.indexOf(c);
                if (value >= 0) {
                    acc = (acc << 5) | value;
                    bits += 5;

                    if (bits >= 8) {
                        buffer.put((byte) ((acc >> (bits - 8)) & 0xFF));
                        bits -= 8;
                    }
                }
            }

            byte[] result = new byte[buffer.position()];
            buffer.flip();
            buffer.get(result);
            return result;
        }
    }
}