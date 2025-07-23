package com.moneymapper.budgettracker.service;

import com.moneymapper.budgettracker.domain.User;
import com.moneymapper.budgettracker.domain.Expense;
import com.moneymapper.budgettracker.dto.DashboardDTO;
import com.moneymapper.budgettracker.dto.ExpenseReadDTO;
import com.moneymapper.budgettracker.repository.ExpenseRepository;
import com.moneymapper.budgettracker.repository.BudgetRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.awt.print.Pageable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;

    public DashboardDTO getDashboardData(User user) {
        Long userId = user.getId();

        BigDecimal totalExpenses = expenseRepository.sumTotalAmountByUser(userId);

        int month = LocalDate.now().getMonthValue();
        int year = LocalDate.now().getYear();
        BigDecimal monthlyBudget = budgetRepository.findActiveBudgetLimits(userId, LocalDate.now())
                .stream()
                .findFirst()
                .orElse(BigDecimal.ZERO);

        BigDecimal budgetUsed = totalExpenses.compareTo(monthlyBudget) > 0 ? monthlyBudget : totalExpenses;
        BigDecimal budgetRemaining = monthlyBudget.subtract(totalExpenses);

        List<Expense> recentExpenses = expenseRepository.findByUserIdOrderByExpenseDateDesc(userId, (Pageable) PageRequest.of(0, 5));
        List<ExpenseReadDTO> recentTransactions = recentExpenses.stream()
                .map(e -> new ExpenseReadDTO(
                        e.getId(),
                        e.getDescription(),
                        e.getAmount(),
                        e.getCategory().getName(),
                        e.getExpenseDate()
                ))
                .toList();

        return new DashboardDTO(
                totalExpenses,
                monthlyBudget,
                budgetUsed,
                budgetRemaining,
                recentTransactions
        );
    }
}