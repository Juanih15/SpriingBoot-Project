package com.moneymapper.budgettracker.service;

import com.moneymapper.budgettracker.domain.*;
import com.moneymapper.budgettracker.domain.Expense;
import com.moneymapper.budgettracker.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;

@Service
public class BudgetService {
    private final ExpenseRepository repo;
    private final Clock clock;

    public BudgetService(ExpenseRepository repo, Clock clock) {
        this.repo = repo;
        this.clock = clock;
    }

    @Transactional
    public Expense addExpense(Category cat, BigDecimal amt) {
        return repo.save(new Expense(cat, amt, LocalDate.now(clock)));
    }

    @Transactional(readOnly = true)
    public Map<Category, BigDecimal> totals() {
        Map<Category, BigDecimal> map = new EnumMap<>(Category.class);
        repo.sumByCategory().forEach(r -> map.put((Category) r[0], (BigDecimal) r[1]));
        return map;
    }
}