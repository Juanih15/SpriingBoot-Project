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
                        select c.id, c.name, coalesce(c.parent.id, c.id) as bucket, sum(e.amount)
                        from Expense e join e.category c
                        where e.budget.owner.id = :uid
                        group by bucket, c.id, c.name
                     """)
       List<Object[]> sumByCategoryForUser(@Param("uid") Long userId);

       @EntityGraph(attributePaths = { "category", "budget" })
       List<Expense> findByBudget(Budget b);

}
