package com.moneymapper.budgettracker;

import com.moneymapper.budgettracker.domain.*;
import com.moneymapper.budgettracker.repository.ExpenseRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ExpenseRepositoryTest {
    @Autowired
    ExpenseRepository repo;

    @Test
    void saveAndSum() {
        repo.save(new Expense(Category.GAS, new BigDecimal("40.00"), LocalDate.now()));
        repo.save(new Expense(Category.GAS, new BigDecimal("10.00"), LocalDate.now()));

        BigDecimal sum = (BigDecimal) repo.sumByCategory().get(0)[1];
        assertThat(sum).isEqualByComparingTo("50.00");
    }

}