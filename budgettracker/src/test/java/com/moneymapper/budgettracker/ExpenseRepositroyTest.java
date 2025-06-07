package com.moneymapper.budgettracker;

import com.moneymapper.budgettracker.domain.*;
import com.moneymapper.budgettracker.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the owner-scoped queries in {@link ExpenseRepository}.
 *
 * Uses @DataJpaTest so Spring spins up an in-memory H2 database automatically.
 */
@DataJpaTest
class ExpenseRepositoryTest {

        @Autowired
        UserRepository userRepo;
        @Autowired
        BudgetRepository budgetRepo;
        @Autowired
        CategoryRepository categoryRepo;
        @Autowired
        ExpenseRepository expenseRepo;

        private final PasswordEncoder encoder = new BCryptPasswordEncoder();

        @Test
        @DisplayName("findByBudget_Owner returns only expenses that belong to that owner")
        void findsExpensesForOwner() {

                // ---------- GIVEN ----------
                User alice = userRepo.save(
                                new User("alice", encoder.encode("secret"), Set.of("ROLE_USER")));
                User bob = userRepo.save(
                                new User("bob", encoder.encode("secret"), Set.of("ROLE_USER")));

                Budget aliceBudget = new Budget("April", BigDecimal.valueOf(1_000),
                                LocalDate.of(2025, 4, 1),
                                LocalDate.of(2025, 4, 30));
                aliceBudget.setOwner(alice); // <<< add the back-pointer
                budgetRepo.save(aliceBudget);

                Budget bobBudget = new Budget("April", BigDecimal.valueOf(1_200),
                                LocalDate.of(2025, 4, 1),
                                LocalDate.of(2025, 4, 30));
                bobBudget.setOwner(bob);
                budgetRepo.save(bobBudget);

                Category groceries = categoryRepo.save(new Category("Groceries"));
                Category rent = categoryRepo.save(new Category("Rent"));

                // create expenses *via* the helper so relationships stay in sync
                Expense e1 = new Expense(groceries, "Whole Foods", BigDecimal.valueOf(50),
                                LocalDate.of(2025, 4, 5));
                aliceBudget.addExpense(e1); // sets e1.setBudget(...)

                Expense e2 = new Expense(rent, "Landlord", BigDecimal.valueOf(700),
                                LocalDate.of(2025, 4, 1));
                bobBudget.addExpense(e2);

                // JPA cascades from budget → expense because of CascadeType.ALL
                budgetRepo.saveAll(List.of(aliceBudget, bobBudget));

                // ---------- WHEN ----------
                List<Expense> found = expenseRepo.findByBudget_Owner(alice);

                // ---------- THEN ----------
                assertThat(found)
                                .hasSize(1)
                                .first()
                                .matches(exp -> exp.getDescription().equals("Whole Foods")
                                                && exp.getBudget().getOwner().equals(alice));
        }
}
