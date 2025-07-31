package com.moneymapper.budgettracker.mapper;

import com.moneymapper.budgettracker.domain.Category;
import com.moneymapper.budgettracker.domain.Expense;
import com.moneymapper.budgettracker.domain.User;
import com.moneymapper.budgettracker.dto.ExpenseDTO;


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

    public static Expense fromDto(ExpenseDTO dto, Category category, User user) {
        Expense expense = new Expense(category, dto.description(), dto.amount(), dto.expenseDate());
        expense.setMemo(dto.memo());
        expense.setUser(user);
        return expense;
    }


}
