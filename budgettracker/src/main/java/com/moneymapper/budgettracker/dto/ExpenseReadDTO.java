package com.moneymapper.budgettracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseReadDTO(
        Long id,
        String description,
        BigDecimal amount,
        String categoryName,
        LocalDate expenseDate
) {}