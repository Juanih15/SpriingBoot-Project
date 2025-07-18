package com.moneymapper.budgettracker.mapper;

import com.moneymapper.budgettracker.domain.User;
import com.moneymapper.budgettracker.dto.CreateUserRequest;
import com.moneymapper.budgettracker.dto.RegistrationRequest;
import com.moneymapper.budgettracker.dto.UpdateUserRequest;
import com.moneymapper.budgettracker.dto.UserDTO;
import com.moneymapper.budgettracker.dto.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class UserMapper {

    /**
     * Convert User entity to UserDTO
     */
    public static UserDTO toUserDTO(User user) {
        if (user == null) {
            return null;
        }

        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.isEnabled(),
                user.getRoles(),
                user.getCreatedAt(),
                user.getLastLoginAt()
        );
    }

    /**
     * Convert User entity to UserResponse DTO (for backward compatibility)
     */
    public static UserResponse toUserResponse(User user) {
        if (user == null) {
            return null;
        }

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.isEnabled(),
                user.getRoles(),
                user.getCreatedAt(),
                user.getLastLoginAt()
        );
    }

    /**
     * Convert CreateUserRequest to User entity
     */
    public static User fromCreateUserRequest(CreateUserRequest request, PasswordEncoder passwordEncoder) {
        if (request == null) {
            return null;
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEmail(request.email());
        user.setEnabled(true); // Enable by default for admin creation

        // Set roles if provided, otherwise use default
        if (request.roles() != null && !request.roles().isEmpty()) {
            user.setRoles(new HashSet<>(request.roles()));
        } else {
            user.setRoles(Set.of("ROLE_USER"));
        }

        return user;
    }

    /**
     * Convert RegistrationRequest to User entity
     */
    public static User fromRegistrationRequest(RegistrationRequest request, PasswordEncoder passwordEncoder) {
        if (request == null) {
            return null;
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEmail(request.email());
        user.setEnabled(false); // Will be enabled after email verification

        // Set roles if provided, otherwise use default
        if (request.roles() != null && !request.roles().isEmpty()) {
            user.setRoles(new HashSet<>(request.roles()));
        } else {
            user.setRoles(Set.of("ROLE_USER"));
        }

        return user;
    }

    /**
     * Update existing User entity with data from UpdateUserRequest
     * Note: This method doesn't handle password updates
     */
    public static void updateUserFromRequest(User user, UpdateUserRequest request) {
        if (user == null || request == null) {
            return;
        }

        if (request.username() != null) {
            user.setUsername(request.username());
        }

        if (request.email() != null) {
            user.setEmail(request.email());
        }

        if (request.enabled() != null) {
            user.setEnabled(request.enabled());
        }

        if (request.roles() != null) {
            user.setRoles(new HashSet<>(request.roles()));
        }
    }
}