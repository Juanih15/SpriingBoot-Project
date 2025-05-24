package com.moneymapper.budgettracker;

import com.moneymapper.budgettracker.domain.*;
import com.moneymapper.budgettracker.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
@DataJpaTest
class ExpenseRepositoryTest {

    @Autowired
    ExpenseRepository repo;
    @Autowired
    CategoryRepository categoryRepo;
    @Autowired
    BudgetRepository budgetRepo;
    @Autowired
    UserRepository userRepo;

    @Test
    void saveAndSum() {

        var user = userRepo.save(new User("tester", "nopass"));
        var cat = categoryRepo.save(new Category("GAS", null, null));

        var budget = budgetRepo.save(new Budget(user, "May",
                new BigDecimal("1000"), LocalDate.now(), LocalDate.now()));

        repo.save(new Expense(cat, budget, new BigDecimal("40.00"), LocalDate.now()));
        repo.save(new Expense(cat, budget, new BigDecimal("10.00"), LocalDate.now()));

        var row = repo.sumByCategoryForUser(user.getId()).get(0);
        var total = (BigDecimal) row[3]; // index 3 = SUM(e.amount)
        assertEquals(new BigDecimal("50.00"), total);

    }
}
