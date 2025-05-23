package com.moneymapper.budgettracker.dto;

import java.math.BigDecimal;

public record NewExpenseDTO(
                BigDecimal amount,
                Long categoryId,
                String memo) {
}