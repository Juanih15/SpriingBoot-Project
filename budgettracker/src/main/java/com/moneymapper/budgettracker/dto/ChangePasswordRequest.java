package com.moneymapper.budgettracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "Current password is required")
        String currentPassword,

        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "New password must be at least 8 characters")
        String newPassword,

        @NotBlank(message = "Password confirmation is required")
        String confirmPassword
) {
        public boolean isPasswordMatching() {
                return newPassword != null && newPassword.equals(confirmPassword);
        }
}