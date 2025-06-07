package com.moneymapper.budgettracker.config;

import com.moneymapper.budgettracker.domain.User;
import com.moneymapper.budgettracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.CommandLineRunner;

import java.util.Set;

@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class SeedUsers implements CommandLineRunner {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        userRepo.findByUsername("admin").ifPresentOrElse(
                u -> {
                }, // already seeded
                () -> userRepo.save(
                        new User("admin",
                                encoder.encode("admin123"),
                                Set.of("ROLE_ADMIN"))));
    }
}
