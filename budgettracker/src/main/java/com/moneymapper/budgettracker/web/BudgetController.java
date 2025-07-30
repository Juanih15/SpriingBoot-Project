package com.moneymapper.budgettracker.web;

import com.moneymapper.budgettracker.dto.*;
import com.moneymapper.budgettracker.service.BudgetService;
import com.moneymapper.budgettracker.domain.User;
import com.moneymapper.budgettracker.domain.Budget;
import com.moneymapper.budgettracker.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
@Slf4j
public class BudgetController {

    private final BudgetService budgetService;
    private final DashboardService dashboardService;

    @PostMapping("/expenses")
    public ResponseEntity<ApiResponse<ExpenseDTO>> addExpense(
            @RequestBody ExpenseDTO body,
            @AuthenticationPrincipal User user) {
        try {
            ExpenseDTO dto = budgetService.addExpense(body, user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Expense added successfully", dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/expenses/current")
    public ResponseEntity<ApiResponse<ExpenseDTO>> addExpenseForCurrentUser(@RequestBody ExpenseDTO body) {
        try {
            ExpenseDTO dto = budgetService.addExpense(body);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Expense added successfully", dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/totals")
    public ResponseEntity<ApiResponse<Map<CategoryDto, BigDecimal>>> getCurrentUserTotals() {
        try {
            Map<CategoryDto, BigDecimal> totals = budgetService.totals();
            return ResponseEntity.ok(ApiResponse.success(totals));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<Budget>> getActiveBudget() {
        try {
            Budget budget = budgetService.getActiveBudget();
            return ResponseEntity.ok(ApiResponse.success(budget));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Budget>>> getUserBudgets() {
        try {
            List<Budget> userBudgets = budgetService.getUserBudgets();
            return ResponseEntity.ok(ApiResponse.success(userBudgets));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardDTO>> getDashboardData(@AuthenticationPrincipal User user) {
        try {
            log.info("Dashboard endpoint called for user: {}", user != null ? user.getUsername() : "null");

            if (user == null) {
                log.error("User is null in dashboard endpoint");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("User not authenticated"));
            }

            log.info("Calling dashboard service for user ID: {}", user.getId());
            DashboardDTO dashboardData = dashboardService.getDashboardData(user);
            log.info("Dashboard data retrieved successfully");

            return ResponseEntity.ok(ApiResponse.success(dashboardData));
        } catch (Exception e) {
            log.error("Error in dashboard endpoint: ", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Dashboard error: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Budget>> createBudget(@RequestBody CreateBudgetRequest request) {
        try {
            Budget budget = budgetService.createBudget(
                    request.name(),
                    request.limit(),
                    request.startDate(),
                    request.endDate()
            );
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Budget created successfully", budget));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}