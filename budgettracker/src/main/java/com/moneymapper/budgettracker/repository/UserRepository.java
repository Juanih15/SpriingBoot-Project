package com.moneymapper.budgettracker.repository;

import com.moneymapper.budgettracker.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {


    Optional<User> findByUsername(String username);


    Optional<User> findByEmail(String email);


    @Query("SELECT u FROM User u WHERE u.username = :usernameOrEmail OR u.email = :usernameOrEmail")
    Optional<User> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);


    boolean existsByUsername(String username);


    boolean existsByEmail(String email);


    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role")
    List<User> findByRole(@Param("role") String role);


    @Query("SELECT u FROM User u WHERE u.enabled = true")
    List<User> findEnabledUsers();

    @Query("SELECT u FROM User u WHERE u.createdAt >= :date")
    List<User> findUsersCreatedAfter(@Param("date") LocalDateTime date);

    List<User> findByEnabled(boolean enabled);

    long countByEnabled(boolean enabled);

    @Query("SELECT u FROM User u WHERE u.email LIKE %:domain")
    List<User> findByEmailDomain(@Param("domain") String domain);


    interface UserCredentialsView {
        String getUsername();
        String getPassword();
        boolean isEnabled();
        Set<String> getRoles();
    }

    Optional<UserCredentialsView> findProjectionByUsername(String username);
}