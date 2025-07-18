package com.moneymapper.budgettracker.repository;

import com.moneymapper.budgettracker.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TwoFactorAuthRepository extends JpaRepository<TwoFactorAuth, Long> {

    Optional<TwoFactorAuth> findByUser(User user);

    Optional<TwoFactorAuth> findByUserId(Long userId);

    boolean existsByUser(User user);

    @Query("SELECT COUNT(t) FROM TwoFactorAuth t WHERE t.enabled = true")
    long countEnabledUsers();

    void deleteByUser(User user);
}