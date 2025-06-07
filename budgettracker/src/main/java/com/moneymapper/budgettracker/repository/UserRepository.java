package com.moneymapper.budgettracker.repository;

import com.moneymapper.budgettracker.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.Set;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<UserCredentialsView> findProjectionByUsername(String username);

    interface UserCredentialsView {
        String getPassword();

        boolean isEnabled();

        Set<String> getRoles();
    }

}
