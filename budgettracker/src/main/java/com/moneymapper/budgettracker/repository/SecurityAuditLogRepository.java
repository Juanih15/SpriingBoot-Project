package com.moneymapper.budgettracker.repository;
import com.moneymapper.budgettracker.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SecurityAuditLogRepository extends JpaRepository<SecurityAuditLog, Long> {

    List<SecurityAuditLog> findByUsernameOrderByTimestampDesc(String username);

    List<SecurityAuditLog> findByActionOrderByTimestampDesc(SecurityAuditLog.SecurityAction action);

    List<SecurityAuditLog> findByIpAddressOrderByTimestampDesc(String ipAddress);

    @Query("SELECT s FROM SecurityAuditLog s WHERE s.username = :username AND s.timestamp > :since ORDER BY s.timestamp DESC")
    List<SecurityAuditLog> findRecentByUsername(@Param("username") String username, @Param("since") LocalDateTime since);

    @Query("SELECT s FROM SecurityAuditLog s WHERE s.action = :action AND s.status = :status AND s.timestamp > :since ORDER BY s.timestamp DESC")
    List<SecurityAuditLog> findByActionAndStatusAfter(@Param("action") SecurityAuditLog.SecurityAction action,
                                                      @Param("status") SecurityAuditLog.SecurityEventStatus status,
                                                      @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(s) FROM SecurityAuditLog s WHERE s.username = :username AND s.action = :action AND s.status = :status AND s.timestamp > :since")
    long countFailedAttempts(@Param("username") String username,
                             @Param("action") SecurityAuditLog.SecurityAction action,
                             @Param("status") SecurityAuditLog.SecurityEventStatus status,
                             @Param("since") LocalDateTime since);

    @Modifying
    @Query("DELETE FROM SecurityAuditLog s WHERE s.timestamp < :cutoff")
    void deleteOldLogs(@Param("cutoff") LocalDateTime cutoff);
}