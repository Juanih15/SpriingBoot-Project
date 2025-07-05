package com.moneymapper.budgettracker.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseDTO(
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        BigDecimal amount,

        @NotBlank(message = "Description is required")
        @Size(max = 120, message = "Description cannot exceed 120 characters")
        String description,

        @NotNull(message = "Expense date is required")
        @PastOrPresent(message = "Expense date cannot be in the future")
        LocalDate expenseDate,

        @Size(max = 255, message = "Memo cannot exceed 255 characters")
        String memo,

        @NotNull(message = "Category ID is required")
        Long categoryId,

        Long userId
) {}