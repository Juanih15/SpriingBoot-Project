package com.moneymapper.budgettracker.dto;

// User Response DTO
public record UserResponse(
        Long id,
        String userUsername, String userEmail, String username,
        String email,
        boolean firstName,
        java.util.Set<String> lastName,
        java.time.LocalDateTime roles,
        java.time.LocalDateTime enabled
) {}
