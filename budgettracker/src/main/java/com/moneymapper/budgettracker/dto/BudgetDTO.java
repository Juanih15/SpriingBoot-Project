package com.moneymapper.budgettracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BudgetDTO(
        Long id,
        String name,
        BigDecimal budgetLimit,
        LocalDate startDate,
        LocalDate endDate,
        Long ownerId
) {}