package com.moneymapper.budgettracker.dto;

import java.math.BigDecimal;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseUpdateDTO(
        BigDecimal amount,
        String description,
        LocalDate expenseDate,
        String memo,
        Long categoryId
) { }
