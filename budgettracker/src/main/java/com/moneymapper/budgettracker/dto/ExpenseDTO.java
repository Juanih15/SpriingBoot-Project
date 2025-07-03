package com.moneymapper.budgettracker.dto;

import java.math.BigDecimal;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseDTO(
        BigDecimal amount,
        String description,
        LocalDate expenseDate,
        String memo,
        Long categoryId,
        Long userId) {
}
// This record represents an expense with its amount, description, date, category ID, and user ID.