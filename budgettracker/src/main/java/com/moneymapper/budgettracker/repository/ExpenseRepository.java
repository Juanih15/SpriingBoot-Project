package com.moneymapper.budgettracker.repository;

import com.moneymapper.budgettracker.domain.Budget;
import com.moneymapper.budgettracker.domain.Expense;
import com.moneymapper.budgettracker.domain.User;

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

       @Query("""
                     SELECT c.id                      AS categoryId,
                            c.name                    AS categoryName,
                            COALESCE(c.parent.id,c.id) AS bucketId,
                            SUM(e.amount)             AS total
                     FROM   Expense e
                            JOIN e.category c
                            JOIN e.budget   b
                     WHERE  b.owner.id = :userId
                     GROUP  BY c.id, c.name, c.parent.id
                     """)
       List<Object[]> sumByCategoryForUser(@Param("userId") Long userId);

       @Query("SELECT e.category.name, SUM(e.amount) "
                     + "FROM Expense e GROUP BY e.category.name")
       List<Object[]> sumByCategoryName();

       List<Expense> findByBudget_Owner(User owner);

       @Query("select e from Expense e where e.budget.id = :budgetId and e.budget.owner = :owner")
       List<Expense> findByBudget(@Param("budgetId") Long budgetId,
                     @Param("owner") User owner);

}
