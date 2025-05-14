package com.moneymapper.budgettracker;

import com.moneymapper.budgettracker.bootstrap.CategorySeeder;
import com.moneymapper.budgettracker.domain.*;
import com.moneymapper.budgettracker.repository.CategoryRepository;
import com.moneymapper.budgettracker.repository.ExpenseRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(CategorySeeder.class)
class ExpenseRepositoryTest {

    @Autowired
    CategoryRepository categories;
    @Autowired
    ExpenseRepository expenses;

    @Test
    void sumByBucket_rollsUpChildrenIntoParent() {
        // build a tree
        Category living = categories.save(new Category("Living Expenses", null, null));
        Category water = categories.save(new Category("Water", living, null));
        Category rent = categories.save(new Category("Rent / Mortgage", living, null));

        // two expenses under different children
        expenses.save(new Expense(water, new BigDecimal("55"), LocalDate.now()));
        expenses.save(new Expense(rent, new BigDecimal("700"), LocalDate.now()));

        // query & map
        Map<Long, BigDecimal> totals = expenses.sumByBucket((Long) null) // cast resolves overload
                .stream() // must call stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(), // bucket_id
                        row -> (BigDecimal) row[1])); // total

        assertThat(totals.get(living.getId()))
                .isEqualByComparingTo("755.00");
    }

}
