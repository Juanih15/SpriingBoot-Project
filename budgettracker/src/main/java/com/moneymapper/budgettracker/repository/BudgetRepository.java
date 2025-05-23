package com.moneymapper.budgettracker.repository;

import com.moneymapper.budgettracker.domain.Budget;
import com.moneymapper.budgettracker.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findTopByOwnerOrderByStartDateDesc(User owner);
}
