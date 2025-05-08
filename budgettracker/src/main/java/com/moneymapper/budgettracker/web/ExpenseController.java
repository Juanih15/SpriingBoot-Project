package com.moneymapper.budgettracker.web;

import com.moneymapper.budgettracker.domain.User;
import com.moneymapper.budgettracker.service.BudgetService;
import com.moneymapper.budgettracker.web.dto.CategoryDto;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ExpenseController {

    private final BudgetService budget;

    public ExpenseController(BudgetService budget) {
        this.budget = budget;
    }

    @GetMapping("/summary")
    public Map<CategoryDto, BigDecimal> summary(@AuthenticationPrincipal User user) {
        // user will be null until finish JWT/session work
        return budget.totals(user);
    }
}
