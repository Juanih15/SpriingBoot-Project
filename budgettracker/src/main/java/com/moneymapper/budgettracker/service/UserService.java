package com.moneymapper.budgettracker.service;

import com.moneymapper.budgettracker.domain.User;
import com.moneymapper.budgettracker.dto.*;
import com.moneymapper.budgettracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    // Existing methods
    public User register(String username, String rawPassword) {
        User user = new User(username, passwordEncoder.encode(rawPassword));
        return userRepo.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No such user: " + username));
    }

    public User getCurrentUser() {
        // TODO: hook into SecurityContextHolder later
        return null;
    }

    // New CRUD methods
    public UserDTO createUser(CreateUserRequest request) {
        // Check if username already exists
        if (userRepo.findByUsername(request.username()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        User user = new User(request.username(), passwordEncoder.encode(request.password()));
        User saved = userRepo.save(user);
        return toUserDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepo.findAll().stream()
                .map(this::toUserDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return toUserDTO(user);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserByUsername(String username) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return toUserDTO(user);
    }

    public UserDTO updateUser(Long id, UpdateUserRequest request) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Update fields if provided
        if (request.username() != null) {
            // Check if new username is already taken by another user
            userRepo.findByUsername(request.username())
                    .filter(existingUser -> !existingUser.getId().equals(id))
                    .ifPresent(existingUser -> {
                        throw new IllegalArgumentException("Username already exists");
                    });
            user.setUsername(request.username());
        }

        if (request.enabled() != null) {
            user.setEnabled(request.enabled());
        }

        User updated = userRepo.save(user);
        return toUserDTO(updated);
    }

    public void deleteUser(Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        userRepo.delete(user);
    }

    public void changePassword(Long id, ChangePasswordRequest request) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Set new password
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepo.save(user);
    }

    public void enableUser(Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setEnabled(true);
        userRepo.save(user);
    }

    public void disableUser(Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setEnabled(false);
        userRepo.save(user);
    }

    // Helper methods
    private UserDTO toUserDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.isEnabled(),
                user.getRoles()
        );
    }
}