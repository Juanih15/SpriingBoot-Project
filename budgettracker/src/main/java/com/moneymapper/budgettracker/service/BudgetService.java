package com.moneymapper.budgettracker.service;

import com.moneymapper.budgettracker.domain.*;
import com.moneymapper.budgettracker.dto.*;
import com.moneymapper.budgettracker.mapper.ExpenseMapper;
import com.moneymapper.budgettracker.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BudgetService {

    private final BudgetRepository budgetRepo;
    private final CategoryRepository categoryRepo;
    private final ExpenseRepository expenseRepo;
    private final Clock clock = Clock.systemDefaultZone();

    public ExpenseDTO addExpense(NewExpenseDTO dto, User owner) {

        Category cat = categoryRepo.findById(dto.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("category not found"));

        Budget active = findActiveBudgetFor(owner);

        Expense exp = new Expense(cat, active, dto.amount(), LocalDate.now(clock));
        exp.setMemo(dto.memo());

        Expense saved = expenseRepo.save(exp);
        return ExpenseMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public Map<CategoryDto, BigDecimal> totals(User owner) {
        List<Object[]> rows = expenseRepo.sumByCategoryForUser(owner.getId());
        return rows.stream()
                .collect(Collectors.toMap(
                        r -> new CategoryDto((Long) r[0], (String) r[1], (Long) r[2]),
                        r -> (BigDecimal) r[3]));
    }

    // New CRUD methods
    public BudgetDTO createBudget(BudgetRequest request, User owner) {
        Budget budget = new Budget(request.name(), request.budgetLimit(),
                request.startDate(), request.endDate());
        budget.setOwner(owner);

        Budget saved = budgetRepo.save(budget);
        return toBudgetDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<BudgetDTO> getUserBudgets(User owner) {
        List<Budget> budgets = budgetRepo.findAllByOwnerOrderByStartDateDesc(owner);
        return budgets.stream()
                .map(this::toBudgetDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BudgetDTO getBudget(Long id, User owner) {
        Budget budget = budgetRepo.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new IllegalArgumentException("Budget not found or access denied"));
        return toBudgetDTO(budget);
    }

    public BudgetDTO updateBudget(Long id, UpdateBudgetRequest request, User owner) {
        Budget budget = budgetRepo.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new IllegalArgumentException("Budget not found or access denied"));

        // Update fields if provided
        if (request.name() != null) {
            budget.setName(request.name());
        }
        if (request.budgetLimit() != null) {
            budget.setBudgetLimit(request.budgetLimit());
        }
        if (request.startDate() != null) {
            budget.setStartDate(request.startDate());
        }
        if (request.endDate() != null) {
            budget.setEndDate(request.endDate());
        }

        Budget updated = budgetRepo.save(budget);
        return toBudgetDTO(updated);
    }

    public void deleteBudget(Long id, User owner) {
        Budget budget = budgetRepo.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new IllegalArgumentException("Budget not found or access denied"));

        budgetRepo.delete(budget);
    }

    @Transactional(readOnly = true)
    public BudgetWithExpenseDTO getBudgetWithExpenses(Long id, User owner) {
        Budget budget = budgetRepo.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new IllegalArgumentException("Budget not found or access denied"));

        List<ExpenseDTO> expenses = budget.getExpenses().stream()
                .map(ExpenseMapper::toDto)
                .collect(Collectors.toList());

        BigDecimal totalSpent = budget.getExpenses().stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remaining = budget.getBudgetLimit().subtract(totalSpent);

        return new BudgetWithExpenseDTO(
                budget.getId(),
                budget.getName(),
                budget.getBudgetLimit(),
                budget.getStartDate(),
                budget.getEndDate(),
                expenses,
                totalSpent,
                remaining);
    }

    // Helper methods
    private Budget findActiveBudgetFor(User owner) {
        return budgetRepo.findTopByOwnerOrderByStartDateDesc(owner)
                .orElseThrow(() -> new IllegalStateException("No budget defined"));
    }

    private BudgetDTO toBudgetDTO(Budget budget) {
        return new BudgetDTO(
                budget.getId(),
                budget.getName(),
                budget.getBudgetLimit(),
                budget.getStartDate(),
                budget.getEndDate(),
                budget.getOwner().getId());
    }
}