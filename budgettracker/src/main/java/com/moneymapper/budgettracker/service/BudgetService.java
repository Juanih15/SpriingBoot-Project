package com.moneymapper.budgettracker.service;

import com.moneymapper.budgettracker.domain.*;
import com.moneymapper.budgettracker.dto.*;
import com.moneymapper.budgettracker.mapper.ExpenseMapper;
import com.moneymapper.budgettracker.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
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
        return ExpenseMapper.toDto(saved); // static mapper you already have
    }

    public Map<CategoryDto, BigDecimal> totals(User owner) {
        List<Object[]> rows = expenseRepo.sumByCategoryForUser(owner.getId());
        return rows.stream()
                .collect(Collectors.toMap(
                        r -> new CategoryDto((Long) r[0], (String) r[1], (Long) r[2]),
                        r -> (BigDecimal) r[3]));
    }

    private Budget findActiveBudgetFor(User owner) {
        return budgetRepo.findTopByOwnerOrderByStartDateDesc(owner)
                .orElseThrow(() -> new IllegalStateException("No budget defined"));
    }
}
