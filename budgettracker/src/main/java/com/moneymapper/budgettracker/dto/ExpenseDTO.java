package com.moneymapper.budgettracker.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ExpenseDTO(
                Long id,
                BigDecimal amount,
                String memo,
                String categoryName) {
}
