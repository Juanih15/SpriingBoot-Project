package com.moneymapper.budgettracker.mapper;

import com.moneymapper.budgettracker.dto.ExpenseDTO;
import com.moneymapper.budgettracker.domain.Expense;

public final class ExpenseMapper {
    private ExpenseMapper() {
    }

    public static ExpenseDTO toDto(Expense e) {
        return new ExpenseDTO(
                e.getAmount(),
                e.getDescription(),
                e.getExpenseDate(),
                e.getMemo(),
                e.getCategory().getId(),
                e.getUser().getId());
    }
}
