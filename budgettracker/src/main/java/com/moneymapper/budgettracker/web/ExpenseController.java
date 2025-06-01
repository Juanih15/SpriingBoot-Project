package com.moneymapper.budgettracker.web;

import com.moneymapper.budgettracker.domain.Category;
import com.moneymapper.budgettracker.domain.Expense;
import com.moneymapper.budgettracker.repository.ExpenseRepository;
import com.moneymapper.budgettracker.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseRepository expenseRepo;
    private final CategoryService categoryService;

    public ExpenseController(ExpenseRepository expenseRepo,
            CategoryService categoryService) {
        this.expenseRepo = expenseRepo;
        this.categoryService = categoryService;
    }

    /** POST /api/expenses?categoryId={id}&amount={amt} */
    @PostMapping
    public ResponseEntity<Void> addExpense(@RequestParam Long categoryId,
            @RequestParam BigDecimal amount) {
        Category cat = categoryService.findById(categoryId);
        Expense exp = new Expense(cat, amount, LocalDate.now());
        expenseRepo.save(exp);
        return ResponseEntity.ok().build();
    }

    /** GET /api/expenses/summary */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, BigDecimal>> summary() {
        Map<String, BigDecimal> result = new HashMap<>();
        expenseRepo.sumByCategoryName().forEach(r -> {
            String catName = (String) r[0];
            BigDecimal total = (BigDecimal) r[1];
            result.put(catName, total);
        });
        return ResponseEntity.ok(result);
    }
}
