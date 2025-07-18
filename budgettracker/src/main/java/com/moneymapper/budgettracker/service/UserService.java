package com.moneymapper.budgettracker.service;

import com.moneymapper.budgettracker.domain.User;
import com.moneymapper.budgettracker.dto.*;
import com.moneymapper.budgettracker.mapper.UserMapper;
import com.moneymapper.budgettracker.repository.UserRepository;
import com.moneymapper.budgettracker.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Comprehensive User Service handling all user-related operations
 * including authentication, CRUD operations, and user management
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // ==================== CRUD Operations ====================

    /**
     * Create a new user account (Admin operation)
     */
    public UserDTO createUser(CreateUserRequest request) {
        log.info("Creating new user with username: {}", request.username());

        validateCreateUserRequest(request);
        checkUsernameAvailability(request.username());
        checkEmailAvailability(request.email());

        User user = UserMapper.fromCreateUserRequest(request, passwordEncoder);
        User savedUser = userRepository.save(user);

        log.info("Successfully created user with ID: {} and username: {}", savedUser.getId(), savedUser.getUsername());
        return UserMapper.toUserDTO(savedUser);
    }

    /**
     * Get all users with pagination support
     */
    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        log.debug("Retrieving all users with pagination: {}", pageable);
        return userRepository.findAll(pageable)
                .map(UserMapper::toUserDTO);
    }

    /**
     * Get all users as a simple list (for backward compatibility)
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        log.debug("Retrieving all users as list");
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDTO)
                .toList();
    }

    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        log.debug("Retrieving user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        return UserMapper.toUserDTO(user);
    }

    /**
     * Get user entity by ID (for internal use)
     */
    @Transactional(readOnly = true)
    public User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }

    /**
     * Update user by ID
     */
    public UserDTO updateUser(Long id, UpdateUserRequest request) {
        log.info("Updating user with ID: {}", id);

        User user = getUserEntityById(id);
        validateUpdateUserRequest(request, user);

        // Check for conflicts only if values are changing
        if (request.username() != null && !request.username().equals(user.getUsername())) {
            checkUsernameAvailability(request.username());
        }

        if (request.email() != null && !request.email().equals(user.getEmail())) {
            checkEmailAvailability(request.email());
        }

        UserMapper.updateUserFromRequest(user, request);
        User updatedUser = userRepository.save(user);

        log.info("Successfully updated user with ID: {}", updatedUser.getId());
        return UserMapper.toUserDTO(updatedUser);
    }

    /**
     * Update an existing user entity (for internal use)
     */
    public User updateUser(User user) {
        log.debug("Updating user entity: {}", user.getUsername());
        return userRepository.save(user);
    }

    /**
     * Delete user by ID
     */
    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);

        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found with id: " + id);
        }

        userRepository.deleteById(id);
        log.info("Successfully deleted user with ID: {}", id);
    }

    /**
     * Soft delete user (disable account)
     */
    public void disableUser(Long id) {
        log.info("Disabling user with ID: {}", id);

        User user = getUserEntityById(id);
        user.setEnabled(false);
        userRepository.save(user);

        log.info("Successfully disabled user with ID: {}", id);
    }

    /**
     * Enable user account
     */
    public void enableUser(Long id) {
        log.info("Enabling user with ID: {}", id);

        User user = getUserEntityById(id);
        user.setEnabled(true);
        userRepository.save(user);

        log.info("Successfully enabled user with ID: {}", id);
    }

    // ==================== Authentication & Password Management ====================

    /**
     * Change user password with current password verification
     */
    public void changePassword(Long userId, ChangePasswordRequest request) {
        log.info("Changing password for user ID: {}", userId);

        validateChangePasswordRequest(request);

        User user = getUserEntityById(userId);

        // Verify current password
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        log.info("Successfully changed password for user ID: {}", userId);
    }

    /**
     * Admin reset password (no current password required)
     */
    public void resetPassword(Long userId, String newPassword) {
        log.info("Admin resetting password for user ID: {}", userId);

        if (!StringUtils.hasText(newPassword) || newPassword.length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters long");
        }

        User user = getUserEntityById(userId);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Successfully reset password for user ID: {}", userId);
    }

    /**
     * Update last login timestamp
     */
    public void updateLastLogin(String username) {
        Optional<User> userOpt = findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
            log.debug("Updated last login for user: {}", username);
        }
    }

    // ==================== User Registration (Enhanced Auth) ====================

    /**
     * Register a new user (for enhanced authentication service)
     */
    public User register(RegistrationRequest request) {
        log.info("Registering new user with username: {}", request.username());

        validateRegistrationRequest(request);
        checkUsernameAvailability(request.username());
        checkEmailAvailability(request.email());

        User user = UserMapper.fromRegistrationRequest(request, passwordEncoder);
        User savedUser = userRepository.save(user);

        log.info("Successfully registered user with ID: {} and username: {}", savedUser.getId(), savedUser.getUsername());
        return savedUser;
    }

    // ==================== Search & Query Operations ====================

    /**
     * Find user by username
     */
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        log.debug("Finding user by username: {}", username);
        return userRepository.findByUsername(username);
    }

    /**
     * Find user by email
     */
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    /**
     * Find user by username or email
     */
    @Transactional(readOnly = true)
    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        log.debug("Finding user by username or email: {}", usernameOrEmail);
        return userRepository.findByUsernameOrEmail(usernameOrEmail);
    }

    /**
     * Get users by role
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getUsersByRole(String role) {
        log.debug("Getting users with role: {}", role);
        return userRepository.findByRole(role).stream()
                .map(UserMapper::toUserDTO)
                .toList();
    }

    /**
     * Get enabled users only
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getEnabledUsers() {
        log.debug("Getting enabled users");
        return userRepository.findEnabledUsers().stream()
                .map(UserMapper::toUserDTO)
                .toList();
    }

    // ==================== User Management Operations ====================

    /**
     * Add role to user
     */
    public void addRoleToUser(Long userId, String role) {
        log.info("Adding role {} to user ID: {}", role, userId);

        User user = getUserEntityById(userId);
        user.addRole(role);
        userRepository.save(user);

        log.info("Successfully added role {} to user ID: {}", role, userId);
    }

    /**
     * Remove role from user
     */
    public void removeRoleFromUser(Long userId, String role) {
        log.info("Removing role {} from user ID: {}", role, userId);

        User user = getUserEntityById(userId);
        Set<String> roles = new HashSet<>(user.getRoles());
        roles.remove(role);
        user.setRoles(roles);
        userRepository.save(user);

        log.info("Successfully removed role {} from user ID: {}", role, userId);
    }

    /**
     * Check if user has specific role
     */
    @Transactional(readOnly = true)
    public boolean hasRole(Long userId, String role) {
        User user = getUserEntityById(userId);
        return user.getRoles().contains(role);
    }

    /**
     * Get user statistics
     */
    @Transactional(readOnly = true)
    public UserStatsDTO getUserStatistics() {
        long totalUsers = userRepository.count();
        long enabledUsers = userRepository.countByEnabled(true);
        long disabledUsers = userRepository.countByEnabled(false);

        return new UserStatsDTO(totalUsers, enabledUsers, disabledUsers);
    }

    // ==================== Utility Methods ====================

    /**
     * Get password encoder instance
     */
    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

    /**
     * Get JWT utility instance
     */
    public JwtUtil getJwtUtil() {
        return jwtUtil;
    }

    /**
     * Convert User entity to UserDTO
     */
    public UserDTO toUserDTO(User user) {
        return UserMapper.toUserDTO(user);
    }

    /**
     * Convert User entity to UserResponse DTO (for backward compatibility)
     */
    public UserResponse toUserResponse(User user) {
        return UserMapper.toUserResponse(user);
    }

    /**
     * Check if username exists
     */
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Check if email exists
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // ==================== Spring Security UserDetailsService ====================

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    // ==================== Admin Operations (Missing Methods) ====================

    /**
     * Find all users (for admin operations)
     */
    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        log.debug("Finding all users");
        return userRepository.findAll();
    }

    /**
     * Search users by query string
     */
    @Transactional(readOnly = true)
    public List<User> searchUsers(String query) {
        log.debug("Searching users with query: {}", query);

        if (!StringUtils.hasText(query)) {
            return userRepository.findAll();
        }

        String lowerQuery = query.toLowerCase();
        return userRepository.findAll().stream()
                .filter(user -> user.getUsername().toLowerCase().contains(lowerQuery) ||
                        (user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerQuery)))
                .toList();
    }

    /**
     * Add role to user by username
     */
    public User addRoleToUser(String username, String role) {
        log.info("Adding role {} to user: {}", role, username);

        User user = findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        user.addRole(role);
        User savedUser = userRepository.save(user);

        log.info("Successfully added role {} to user: {}", role, username);
        return savedUser;
    }

    /**
     * Remove role from user by username
     */
    public User removeRoleFromUser(String username, String role) {
        log.info("Removing role {} from user: {}", role, username);

        User user = findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        Set<String> roles = new HashSet<>(user.getRoles());
        roles.remove(role);
        user.setRoles(roles);
        User savedUser = userRepository.save(user);

        log.info("Successfully removed role {} from user: {}", role, username);
        return savedUser;
    }

    /**
     * Set user enabled/disabled status by username
     */
    public User setUserEnabled(String username, boolean enabled) {
        log.info("Setting user {} enabled status to: {}", username, enabled);

        User user = findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        user.setEnabled(enabled);
        User savedUser = userRepository.save(user);

        log.info("Successfully set user {} enabled status to: {}", username, enabled);
        return savedUser;
    }

    /**
     * Delete user by username
     */
    public void deleteUserByUsername(String username) {
        log.info("Deleting user: {}", username);

        User user = findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        userRepository.delete(user);
        log.info("Successfully deleted user: {}", username);
    }

    /**
     * Get current authenticated user or throw exception
     */
    public User getCurrentUserOrThrow() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("No authenticated user found");
        }
        return currentUser;
    }

    /**
     * Get current authenticated user (returns null if not authenticated)
     */
    public User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }

            Object principal = authentication.getPrincipal();

            if (principal instanceof User) {
                return (User) principal;
            }

            if (principal instanceof UserDetails) {
                String username = ((UserDetails) principal).getUsername();
                return findByUsername(username).orElse(null);
            }

            if (principal instanceof String) {
                return findByUsername((String) principal).orElse(null);
            }

            return null;

        } catch (Exception e) {
            log.error("Error getting current user from security context", e);
            return null;
        }
    }

    /**
     * Convert CreateUserRequest to User entity
     */
    private User fromCreateUserRequest(CreateUserRequest request) {
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
    private User fromRegistrationRequest(RegistrationRequest request) {
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


    // Private Validation Methods

    private void validateCreateUserRequest(CreateUserRequest request) {
        if (!StringUtils.hasText(request.username())) {
            throw new IllegalArgumentException("Username is required");
        }
        if (!StringUtils.hasText(request.password())) {
            throw new IllegalArgumentException("Password is required");
        }
        if (request.password().length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
    }

    private void validateUpdateUserRequest(UpdateUserRequest request, User existingUser) {
        if (request.username() != null && !StringUtils.hasText(request.username())) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (request.email() != null && !StringUtils.hasText(request.email())) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
    }

    private void validateRegistrationRequest(RegistrationRequest request) {
        if (!StringUtils.hasText(request.username())) {
            throw new IllegalArgumentException("Username is required");
        }
        if (!StringUtils.hasText(request.password())) {
            throw new IllegalArgumentException("Password is required");
        }
        if (request.password().length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
    }

    private void validateChangePasswordRequest(ChangePasswordRequest request) {
        if (!StringUtils.hasText(request.currentPassword())) {
            throw new IllegalArgumentException("Current password is required");
        }
        if (!StringUtils.hasText(request.newPassword())) {
            throw new IllegalArgumentException("New password is required");
        }
        if (request.newPassword().length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters long");
        }
        if (!request.isPasswordMatching()) {
            throw new IllegalArgumentException("New password and confirmation do not match");
        }
    }

    private void checkUsernameAvailability(String username) {
        if (StringUtils.hasText(username) && userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
    }

    private void checkEmailAvailability(String email) {
        if (StringUtils.hasText(email) && userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }
    }
}

/**
 * DTO for user statistics
 */
record UserStatsDTO(
        long totalUsers,
        long enabledUsers,
        long disabledUsers
) {}