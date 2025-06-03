package com.moneymapper.budgettracker.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.moneymapper.budgettracker.domain.User;
import com.moneymapper.budgettracker.repository.UserRepository;

@Configuration
public class SeedUsers {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public SeedUsers(UserRepository userRepo,
            PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public CommandLineRunner createDefaultUser() {
        return args -> {
            // Only seed “admin” if it doesn’t already exist
            if (userRepo.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                // Always encode the raw password before saving
                admin.setPassword(passwordEncoder.encode("admin123"));

                userRepo.save(admin);
                System.out.println("Created default user: admin / admin123");
            }
        };
    }
}
