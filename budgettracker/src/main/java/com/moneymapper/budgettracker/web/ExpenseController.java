package com.moneymapper.budgettracker.web;

import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;

import com.moneymapper.budgettracker.domain.Category;
import com.moneymapper.budgettracker.service.BudgetService;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ExpenseController {
    private final BudgetService budget;

    public ExpenseController(BudgetService budget) {
        this.budget = budget;
    }

    @PostMapping("/expenses")
    public void add(@RequestParam Category category,
            @RequestParam @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal amount) {
        budget.addExpense(category, amount);
    }

    @GetMapping("/summary")
    public Map<Category, BigDecimal> summary() {
        return budget.totals();
    }
}