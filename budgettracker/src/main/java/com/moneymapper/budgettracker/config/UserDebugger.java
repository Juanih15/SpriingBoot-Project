package com.moneymapper.budgettracker.config;

import com.moneymapper.budgettracker.domain.User;
import com.moneymapper.budgettracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class UserDebugger implements CommandLineRunner {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        System.out.println("\n=== USER DEBUG INFO ===");

        // Check demo user specifically
        userRepo.findByUsername("demo").ifPresentOrElse(
                user -> {
                    System.out.println("Demo user found:");
                    System.out.println("  - ID: " + user.getId());
                    System.out.println("  - Username: " + user.getUsername());
                    System.out.println("  - Email: " + user.getEmail());
                    System.out.println("  - Enabled: " + user.isEnabled());
                    System.out.println("  - Roles: " + user.getRoles());
                    System.out.println("  - Password starts with: " + user.getPassword().substring(0, 10) + "...");

                    // Test password encoding
                    boolean passwordMatches = encoder.matches("demo123", user.getPassword());
                    System.out.println("  - Password 'demo123' matches: " + passwordMatches);

                    // Fix if disabled
                    if (!user.isEnabled()) {
                        System.out.println("FIXING: User was disabled, enabling now...");
                        user.setEnabled(true);
                        userRepo.save(user);
                        System.out.println("User enabled successfully");
                    }

                    // Ensure user has correct roles
                    if (!user.getRoles().contains("ROLE_USER")) {
                        System.out.println("🔧 FIXING: Adding ROLE_USER...");
                        user.setRoles(Set.of("ROLE_USER"));
                        userRepo.save(user);
                        System.out.println("Roles updated");
                    }
                },
                () -> {
                    System.out.println("Demo user NOT found, creating new one...");
                    User demo = new User("demo", encoder.encode("demo123"), Set.of("ROLE_USER"));
                    demo.setEmail("demo@moneymapper.com");
                    demo.setFirstName("Demo");
                    demo.setLastName("User");
                    demo.setEnabled(true);
                    userRepo.save(demo);
                    System.out.println("Created new demo user");
                }
        );

        System.out.println("=== END USER DEBUG ===\n");
    }
}