package com.moneymapper.budgettracker.service;

import com.moneymapper.budgettracker.domain.User;
import com.moneymapper.budgettracker.domain.Expense;
import com.moneymapper.budgettracker.dto.DashboardDTO;
import com.moneymapper.budgettracker.dto.ExpenseReadDTO;
import com.moneymapper.budgettracker.repository.ExpenseRepository;
import com.moneymapper.budgettracker.repository.BudgetRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;

    public DashboardDTO getDashboardData(User user) {
        try {
            Long userId = user.getId();
            log.info("Getting dashboard data for user ID: {}", userId);

            // Get total expenses (handle null)
            BigDecimal totalExpenses = expenseRepository.sumTotalAmountByUser(userId);
            if (totalExpenses == null) {
                totalExpenses = BigDecimal.ZERO;
            }
            log.info("Total expenses for user {}: {}", userId, totalExpenses);

            // Get monthly budget
            List<BigDecimal> budgetLimits = budgetRepository.findActiveBudgetLimits(userId, LocalDate.now());
            BigDecimal monthlyBudget = budgetLimits.stream()
                    .findFirst()
                    .orElse(BigDecimal.ZERO);
            log.info("Monthly budget for user {}: {}", userId, monthlyBudget);

            // Calculate budget used and remaining
            BigDecimal budgetUsed = totalExpenses.min(monthlyBudget);
            BigDecimal budgetRemaining = monthlyBudget.subtract(totalExpenses);
            log.info("Budget used: {}, Budget remaining: {}", budgetUsed, budgetRemaining);

            // Get recent expenses (fix Pageable import issue)
            Pageable pageRequest = PageRequest.of(0, 5);
            List<Expense> recentExpenses;

            try {
                recentExpenses = expenseRepository.findByUserIdOrderByExpenseDateDesc(userId, pageRequest);
            } catch (Exception e) {
                log.warn("Error getting recent expenses, returning empty list: {}", e.getMessage());
                recentExpenses = List.of(); // Return empty list if query fails
            }

            List<ExpenseReadDTO> recentTransactions = recentExpenses.stream()
                    .map(e -> new ExpenseReadDTO(
                            e.getId(),
                            e.getDescription() != null ? e.getDescription() : "No description",
                            e.getAmount(),
                            e.getCategory() != null ? e.getCategory().getName() : "Unknown",
                            e.getExpenseDate() != null ? e.getExpenseDate() : e.getDate()
                    ))
                    .toList();

            log.info("Found {} recent transactions for user {}", recentTransactions.size(), userId);

            DashboardDTO result = new DashboardDTO(
                    totalExpenses,
                    monthlyBudget,
                    budgetUsed,
                    budgetRemaining,
                    recentTransactions
            );

            log.info("Dashboard data created successfully for user {}", userId);
            return result;

        } catch (Exception e) {
            log.error("Error getting dashboard data for user {}: ", user.getId(), e);

            // Return empty/default dashboard data instead of throwing exception
            return new DashboardDTO(
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    List.of()
            );
        }
    }
}