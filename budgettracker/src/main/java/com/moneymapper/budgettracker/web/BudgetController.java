package com.moneymapper.budgettracker.web;

import com.moneymapper.budgettracker.dto.*;
import com.moneymapper.budgettracker.service.BudgetService;
import com.moneymapper.budgettracker.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    // Existing expense endpoint
    @PostMapping("/expenses")
    public ResponseEntity<ExpenseDTO> addExpense(@RequestBody NewExpenseDTO body,
            @AuthenticationPrincipal User user) {
        ExpenseDTO dto = budgets.addExpense(body, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    // Create a new budget
    @PostMapping
    public ResponseEntity<BudgetDTO> createBudget(@Valid @RequestBody BudgetRequest request,
            @AuthenticationPrincipal User user) {
        BudgetDTO created = budgetService.createBudget(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Get all budgets for the current user
    @GetMapping
    public ResponseEntity<List<BudgetDTO>> getUserBudgets(@AuthenticationPrincipal User user) {
        List<BudgetDTO> budgets = budgetService.getUserBudgets(user);
        return ResponseEntity.ok(budgets);
    }

    // Get a specific budget by ID
    @GetMapping("/{id}")
    public ResponseEntity<BudgetDTO> getBudget(@PathVariable Long id,
            @AuthenticationPrincipal User user) {
        BudgetDTO budget = budgetService.getBudget(id, user);
        return ResponseEntity.ok(budget);
    }

    // Update an existing budget
    @PutMapping("/{id}")
    public ResponseEntity<BudgetDTO> updateBudget(@PathVariable Long id,
            @Valid @RequestBody UpdateBudgetRequest request,
            @AuthenticationPrincipal User user) {
        BudgetDTO updated = budgetService.updateBudget(id, request, user);
        return ResponseEntity.ok(updated);
    }

    // Delete a budget
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id,
            @AuthenticationPrincipal User user) {
        budgetService.deleteBudget(id, user);
        return ResponseEntity.noContent().build();
    }

    // Get budget with expenses
    @GetMapping("/{id}/expenses")
    public ResponseEntity<BudgetWithExpenseDTO> getBudgetWithExpenses(@PathVariable Long id,
            @AuthenticationPrincipal User user) {
        BudgetWithExpenseDTO budget = budgetService.getBudgetWithExpenses(id, user);
        return ResponseEntity.ok(budget);
    }
}