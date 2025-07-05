package com.moneymapper.budgettracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/* ---------- Budget ---------- */
public record BudgetDTO(
        Long        id,
        String      name,
        BigDecimal  limit,
        LocalDate   startDate,
        LocalDate   endDate) { }

public record NewBudgetDTO(
        String      name,
        BigDecimal  limit,
        LocalDate   startDate,
        LocalDate   endDate) { }

public record UpdateBudgetDTO(            // partial update
                                          String      name,
                                          BigDecimal  limit,
                                          LocalDate   endDate) { }

/* ---------- Expense ---------- */
public record ExpenseDTO(
        Long        id,
        Long        budgetId,
        String      category,
        String      description,
        BigDecimal  amount,
        LocalDate   date) { }

public record NewExpenseDTO(
        Long        budgetId,
        Long        categoryId,
        String      description,
        BigDecimal  amount,
        LocalDate   date) { }

public record UpdateExpenseDTO(
        String      description,
        BigDecimal  amount,
        LocalDate   date,
        Long        categoryId) { }
