package com.moneymapper.budgettracker.service;

import com.moneymapper.budgettracker.domain.Category;
import com.moneymapper.budgettracker.domain.Expense;
import com.moneymapper.budgettracker.domain.User;
import com.moneymapper.budgettracker.repository.ExpenseRepository;
import com.moneymapper.budgettracker.web.dto.CategoryDto;
import com.moneymapper.budgettracker.web.mapper.CategoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BudgetService {

    private final ExpenseRepository repo;
    private final Clock clock;

    public BudgetService(ExpenseRepository repo, Clock clock) {
        this.repo = repo;
        this.clock = clock;
    }

    @Transactional
    public Expense addExpense(Category category, BigDecimal amount) {
        return repo.save(new Expense(category, amount, LocalDate.now(clock)));
    }

    @Transactional(readOnly = true)
    public Map<CategoryDto, BigDecimal> totals(User user) {
        return repo.sumByBucket(user).stream()
                .collect(Collectors.toMap(
                        r -> CategoryMapper.toDto((Category) r[0]),
                        r -> (BigDecimal) r[1],
                        (a, b) -> a, // merge fn (not expected)
                        LinkedHashMap::new)); // keep insertion order
    }
}
