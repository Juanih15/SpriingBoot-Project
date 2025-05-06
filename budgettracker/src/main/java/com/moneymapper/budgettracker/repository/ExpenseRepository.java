package com.moneymapper.budgettracker.repository;

import com.moneymapper.budgettracker.domain.*;
import org.springframework.data.jpa.repository.*;
import java.math.BigDecimal;
import java.util.List;

@SuppressWarnings("unused")
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    @Query("select e.category, sum(e.amount) from Expense e group by e.category")
    List<Object[]> sumByCategory();
}