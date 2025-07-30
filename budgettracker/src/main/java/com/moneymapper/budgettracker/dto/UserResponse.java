package com.moneymapper.budgettracker.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record UserResponse(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        boolean enabled,
        Set<String> roles,
        LocalDateTime createdAt,
        LocalDateTime lastLoginAt
) {}