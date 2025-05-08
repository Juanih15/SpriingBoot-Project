package com.moneymapper.budgettracker.repository;

import com.moneymapper.budgettracker.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
       @Query(value = """
                     SELECT COALESCE(c.parent_id, c.id) AS bucket_id,
                            SUM(e.amount)              AS total
                     FROM   expense e
                            JOIN categories c ON e.category_id = c.id
                     WHERE  (:userId IS NULL
                             OR c.owner_id = :userId
                             OR c.owner_id IS NULL)
                     GROUP  BY bucket_id
                     """, nativeQuery = true)
       List<Object[]> sumByBucket(@Param("userId") User user);

}
