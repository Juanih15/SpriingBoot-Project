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
        // Create admin user if not exists
        userRepo.findByUsername("admin").ifPresentOrElse(
                u -> System.out.println("Admin user already exists"),
                () -> {
                    User admin = new User("admin", encoder.encode("admin123"), Set.of("ROLE_ADMIN", "ROLE_USER"));
                    admin.setEmail("admin@moneymapper.com");
                    admin.setFirstName("System");
                    admin.setLastName("Administrator");
                    userRepo.save(admin);
                    System.out.println("Created admin user: admin/admin123");
                }
        );

        // Create regular test user if not exists
        userRepo.findByUsername("testuser").ifPresentOrElse(
                u -> System.out.println("Test user already exists"),
                () -> {
                    User testUser = new User("testuser", encoder.encode("password123"), Set.of("ROLE_USER"));
                    testUser.setEmail("test@moneymapper.com");
                    testUser.setFirstName("Test");
                    testUser.setLastName("User");
                    userRepo.save(testUser);
                    System.out.println("Created test user: testuser/password123");
                }
        );

        // Create demo user with sample data
        userRepo.findByUsername("demo").ifPresentOrElse(
                u -> System.out.println("Demo user already exists"),
                () -> {
                    User demoUser = new User("demo", encoder.encode("demo123"), Set.of("ROLE_USER"));
                    demoUser.setEmail("demo@moneymapper.com");
                    demoUser.setFirstName("Demo");
                    demoUser.setLastName("User");
                    userRepo.save(demoUser);
                    System.out.println("Created demo user: demo/demo123");
                }
        );
    }
}