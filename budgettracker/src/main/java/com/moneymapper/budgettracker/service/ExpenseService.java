package com.moneymapper.budgettracker.service;

import com.moneymapper.budgettracker.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepo;

    public ExpenseService(ExpenseRepository expenseRepo) {
        this.expenseRepo = expenseRepo;
    }

    @Transactional
    public int deleteExpensesInTimeFrame(LocalDate startDate, LocalDate endDate) {
        return expenseRepo.deleteByExpenseDateBetween(startDate, endDate);
    }
}
