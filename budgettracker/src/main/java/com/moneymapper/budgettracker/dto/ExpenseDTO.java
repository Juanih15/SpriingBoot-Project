package com.moneymapper.budgettracker.dto;

import java.math.BigDecimal;

public record ExpenseDTO(
                Long id,
                BigDecimal amount,
                String memo,
                String categoryName) {
}
