package com.moneymapper.budgettracker.web;

import com.moneymapper.budgettracker.dto.CategoryDto;
import com.moneymapper.budgettracker.service.BudgetService;
import com.moneymapper.budgettracker.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/summary")
@RequiredArgsConstructor
public class ExpenseController {

    private final BudgetService budgets;

    @GetMapping
    public Map<CategoryDto, BigDecimal> totals(@AuthenticationPrincipal User user) {
        return budgets.totals(user);
    }
}
