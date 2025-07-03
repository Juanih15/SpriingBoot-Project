package com.moneymapper.budgettracker.web;

import com.moneymapper.budgettracker.dto.*;
import com.moneymapper.budgettracker.service.BudgetService;
import com.moneymapper.budgettracker.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgets;

    @PostMapping("/expenses")
    public ResponseEntity<ExpenseDTO> addExpense(@RequestBody ExpenseDTO body,
            @AuthenticationPrincipal User user) {
        ExpenseDTO dto = budgets.addExpense(body, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
}
