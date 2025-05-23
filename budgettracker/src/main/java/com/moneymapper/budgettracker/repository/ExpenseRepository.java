package com.moneymapper.budgettracker.repository;

import com.moneymapper.budgettracker.domain.Budget;
import com.moneymapper.budgettracker.domain.Expense;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

       @Query("""
                     SELECT COALESCE(c.parent.id, c.id) AS bucketId,
                            SUM(e.amount)              AS total
                     FROM   Expense e
                            JOIN e.category c
                     WHERE  (:userId IS NULL
                             OR c.owner.id = :userId
                             OR c.owner IS NULL)
                     GROUP  BY bucketId
                     """)
       List<Object[]> sumByBucket(@Param("userId") Long userId);

       @EntityGraph(attributePaths = { "category", "budget" })
       List<Expense> findByBudget(Budget b);

       List<Object[]> sumByCategoryForUser(Long id);

}
