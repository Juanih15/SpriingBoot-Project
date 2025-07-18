package com.moneymapper.budgettracker.dto;

// JWT Response DTO
public record JwtResponse(
        String token,
        String type,
        String username,
        String email,
        java.util.Set<String> roles
) {
    public JwtResponse(String token, String username, String email, java.util.Set<String> roles) {
        this(token, "Bearer", username, email, roles);
    }
}
