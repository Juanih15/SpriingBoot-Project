package com.moneymapper.budgettracker.repository;

import com.moneymapper.budgettracker.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    @Query("""
            select coalesce(c.parent, c) as bucket,
                   sum(e.amount)         as total
            from   Expense   e
                   join e.category c
            where  :user is null
                   or c.owner = :user
                   or c.owner is null
            group  by bucket
            """)
    List<Object[]> sumByBucket(@Param("user") User user);
}
