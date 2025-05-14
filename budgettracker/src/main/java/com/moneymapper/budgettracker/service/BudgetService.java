package com.moneymapper.budgettracker.service;

import com.moneymapper.budgettracker.domain.User;
import com.moneymapper.budgettracker.repository.CategoryRepository;
import com.moneymapper.budgettracker.repository.ExpenseRepository;
import com.moneymapper.budgettracker.web.dto.CategoryDto;
import com.moneymapper.budgettracker.web.mapper.CategoryMapper;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class BudgetService {

    private final ExpenseRepository expenseRepo;
    private final CategoryRepository categoryRepo;

    public BudgetService(ExpenseRepository expenseRepo,
            CategoryRepository categoryRepo) {
        this.expenseRepo = expenseRepo;
        this.categoryRepo = categoryRepo;
    }

    public Map<CategoryDto, BigDecimal> totals(User user) {
        Map<Long, CategoryDto> byId = categoryRepo.findAll().stream() // uses categoryRepo
                .map(CategoryMapper::toDto)
                .collect(Collectors.toMap(
                        CategoryDto::id,
                        Function.identity()));

        return expenseRepo.sumByBucket(user == null ? null : user.getId()).stream()
                .collect(Collectors.toMap(
                        row -> byId.get(((Number) row[0]).longValue()),
                        row -> (BigDecimal) row[1],
                        (a, b) -> a,
                        LinkedHashMap::new));
    }
}
