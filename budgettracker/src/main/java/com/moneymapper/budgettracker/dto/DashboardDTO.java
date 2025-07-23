package com.moneymapper.budgettracker.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardDTO(
        BigDecimal totalExpenses,
        BigDecimal monthlyBudget,
        BigDecimal budgetUsed,
        BigDecimal budgetRemaining,
        List<ExpenseReadDTO> recentTransactions
) {}
