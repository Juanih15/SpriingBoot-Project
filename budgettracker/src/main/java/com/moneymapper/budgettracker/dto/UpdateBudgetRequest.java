package com.moneymapper.budgettracker.dto;

import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateBudgetRequest(
        String name,

        @Positive(message = "Budget limit must be positive")
        BigDecimal budgetLimit,

        LocalDate startDate,
        LocalDate endDate
) {}