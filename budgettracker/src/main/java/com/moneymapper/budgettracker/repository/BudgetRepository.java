package com.moneymapper.budgettracker.repository;

import com.moneymapper.budgettracker.domain.Budget;
import com.moneymapper.budgettracker.domain.User;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

  // Most-recent period, used by dashboard
  Optional<Budget> findTopByOwnerOrderByStartDateDesc(User owner);

  // Fetch along with expenses in one roundtrip
  @EntityGraph(attributePaths = "expenses")
  Optional<Budget> findByIdAndOwner(Long id, User owner);

  // Get all budgets for a user, ordered by start date (newest first)
  List<Budget> findAllByOwnerOrderByStartDateDesc(User owner);

  // Active budget on a given date
  @Query("""
          select b from Budget b
          where b.owner = :owner
            and (b.startDate is null or :day >= b.startDate)
            and (b.endDate   is null or :day <= b.endDate)
          order by b.startDate desc
          limit 1
      """)
  Optional<Budget> findActiveBudget(@Param("owner") User owner,
                                    @Param("day") LocalDate day);

  @Query("""
    SELECT COALESCE(b.budgetLimit, 0)
    FROM Budget b
    WHERE b.owner.id = :userId
      AND :today BETWEEN b.startDate AND b.endDate
    ORDER BY b.startDate DESC
""")
  List<BigDecimal> findActiveBudgetLimits(@Param("userId") Long userId, @Param("today") LocalDate today);
}