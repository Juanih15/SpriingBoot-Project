package com.moneymapper.budgettracker.dto;

import java.util.Set;

public record UserDTO(
        Long id,
        String username,
        boolean enabled,
        Set<String> roles
) {}