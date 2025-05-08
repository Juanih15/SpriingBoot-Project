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

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(CategorySeeder.class)
class ExpenseRepositoryTest {

    @Autowired
    ExpenseRepository expenses;
    @Autowired
    CategoryRepository categories;

    @Test
    void sumByBucket_rollsUpChildrenIntoParent() {
        Category water = categories.findById(4L).orElseThrow(); // "Water"
        Category rent = categories.findById(2L).orElseThrow(); // "Rent / Mortgage"
        Category living = categories.findById(1L).orElseThrow(); // "Living Expenses" (parent)

        expenses.save(new Expense(water, BigDecimal.valueOf(55), LocalDate.now()));
        expenses.save(new Expense(rent, BigDecimal.valueOf(700), LocalDate.now()));

        var rows = expenses.sumByBucket(null); // null user → include seeded cats

        BigDecimal total = rows.stream()
                .filter(r -> r[0].equals(living))
                .map(r -> (BigDecimal) r[1])
                .findFirst()
                .orElse(BigDecimal.ZERO);

        assertThat(total).isEqualByComparingTo("755.00");
    }
}
