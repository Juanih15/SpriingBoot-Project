package com.moneymapper.budgettracker.repository;

import com.moneymapper.budgettracker.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface JwtTokenBlacklistRepository extends JpaRepository<JwtTokenBlacklist, Long> {

    boolean existsByTokenHash(String tokenHash);

    Optional<JwtTokenBlacklist> findByTokenHash(String tokenHash);

    List<JwtTokenBlacklist> findByUsername(String username);

    @Modifying
    @Query("DELETE FROM JwtTokenBlacklist j WHERE j.expiryDate < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM JwtTokenBlacklist j WHERE j.username = :username AND j.reason = :reason")
    void deleteByUsernameAndReason(@Param("username") String username, @Param("reason") JwtTokenBlacklist.BlacklistReason reason);
}