package com.moneymapper.budgettracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record BudgetWithExpenseDTO(
        Long id,
        String name,
        BigDecimal budgetLimit,
        LocalDate startDate,
        LocalDate endDate,
        List<ExpenseDTO> expenses,
        BigDecimal totalSpent,
        BigDecimal remaining
) {}