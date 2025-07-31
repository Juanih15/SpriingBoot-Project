package com.moneymapper.budgettracker.web;

import com.moneymapper.budgettracker.domain.Category;
import com.moneymapper.budgettracker.domain.Expense;
import com.moneymapper.budgettracker.domain.User;
import com.moneymapper.budgettracker.dto.ExpenseDTO;
import com.moneymapper.budgettracker.mapper.ExpenseMapper;
import com.moneymapper.budgettracker.repository.ExpenseRepository;
import com.moneymapper.budgettracker.service.CategoryService;
import com.moneymapper.budgettracker.dto.ExpenseUpdateDTO;
import com.moneymapper.budgettracker.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseRepository expenseRepo;

    private final ExpenseService expenseService;
    private final CategoryService categoryService;

    public ExpenseController(ExpenseRepository expenseRepo,
            CategoryService categoryService, ExpenseService expenseService) {
        this.expenseRepo = expenseRepo;
        this.categoryService = categoryService;
        this.expenseService = expenseService;
    }

    /** POST /api/expenses */
    @PostMapping
    public ResponseEntity<Void> addExpense(@Valid @RequestBody ExpenseDTO dto,
                                           @AuthenticationPrincipal User principal) {
        Category category = categoryService.findById(dto.categoryId());

        Expense expense = ExpenseMapper.fromDto(dto, category, principal);
        expenseRepo.save(expense);

        return ResponseEntity.ok().build(); // or ResponseEntity.status(HttpStatus.CREATED).build();
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

    /** find all expenses for a given category */
    @GetMapping("/category/{id}")
    public ResponseEntity<Map<String, BigDecimal>> expensesByCategory(@PathVariable Long id) {
        Map<String, BigDecimal> result = new HashMap<>();
        expenseRepo.findByCategoryId(id).forEach(expense -> {
            String catName = expense.getCategory().getName();
            result.put(catName, expense.getAmount());
        });
        return ResponseEntity.ok(result);
    }

    /** DELETE /api/expenses/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        if (!expenseRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        expenseRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /** PATCH /api/expenses/{id} */
    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateExpense(
            @PathVariable Long id,
            @RequestBody ExpenseUpdateDTO dto) {

        Optional<Expense> optionalExpense = expenseRepo.findById(id);
        if (optionalExpense.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Expense expense = optionalExpense.get();

        if (dto.amount() != null) {
            expense.setAmount(dto.amount());
        }
        if (dto.description() != null) {
            expense.setDescription(dto.description());
        }
        if (dto.expenseDate() != null) {
            expense.setExpenseDate(dto.expenseDate());
        }
        if (dto.memo() != null) {
            expense.setMemo(dto.memo());
        }
        if (dto.categoryId() != null) {
            // Retrieve and set new category
            Category cat = categoryService.findById(dto.categoryId());
            expense.setCategory(cat);
        }

        expenseRepo.save(expense);
        return ResponseEntity.noContent().build();
    }

    /** DELETE /api/expenses?startDate={start}&endDate={end} */
    @DeleteMapping
    public ResponseEntity<String> deleteExpensesInTimeFrame(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {

        int deletedCount = expenseService.deleteExpensesInTimeFrame(startDate, endDate);
        return ResponseEntity.ok(deletedCount + " expenses deleted between " + startDate + " and " + endDate);
    }
}
