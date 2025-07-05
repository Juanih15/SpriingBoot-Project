package com.moneymapper.budgettracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public record BudgetRequest(
        @NotBlank(message = "Budget name is required")
        String name,

        @NotNull(message = "Budget limit is required")
        @Positive(message = "Budget limit must be positive")
        BigDecimal budgetLimit,

        @NotNull(message = "Start date is required")
        LocalDate startDate,

        LocalDate endDate // Optional
) {}