package com.moneymapper.budgettracker.web;

import com.moneymapper.budgettracker.dto.*;
import com.moneymapper.budgettracker.domain.User;
import com.moneymapper.budgettracker.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "APIs for managing user accounts")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Create a new user", description = "Creates a new user account (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Username or email already exists"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<com.moneymapper.budgettracker.dto.ApiResponse<UserDTO>> createUser(
            @Valid @RequestBody CreateUserRequest request) {

        log.info("Creating new user with username: {}", request.username());
        UserDTO created = userService.createUser(request);
        log.info("Successfully created user with ID: {}", created.id());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(com.moneymapper.budgettracker.dto.ApiResponse.success("User created successfully", created));
    }

    @Operation(summary = "Get all users", description = "Retrieves all user accounts (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<com.moneymapper.budgettracker.dto.ApiResponse<List<UserDTO>>> getAllUsers() {
        log.info("Retrieving all users");
        List<UserDTO> users = userService.getAllUsers();

        return ResponseEntity.ok(com.moneymapper.budgettracker.dto.ApiResponse.success(users));
    }

    @Operation(summary = "Get current user profile", description = "Retrieves the authenticated user's profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User profile retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<com.moneymapper.budgettracker.dto.ApiResponse<UserDTO>> getCurrentUser(
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {

        log.info("Getting profile for user: {}", user.getUsername());
        UserDTO userDto = userService.getUserById(user.getId());

        return ResponseEntity.ok(com.moneymapper.budgettracker.dto.ApiResponse.success(userDto));
    }

    @Operation(summary = "Get user by ID", description = "Retrieves a specific user by ID (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<com.moneymapper.budgettracker.dto.ApiResponse<UserDTO>> getUser(
            @Parameter(description = "User ID") @PathVariable Long id) {

        log.info("Getting user with ID: {}", id);
        UserDTO user = userService.getUserById(id);

        return ResponseEntity.ok(com.moneymapper.budgettracker.dto.ApiResponse.success(user));
    }

    @Operation(summary = "Update current user", description = "Updates the authenticated user's profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Username or email already exists"),
            @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    @PutMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<com.moneymapper.budgettracker.dto.ApiResponse<UserDTO>> updateCurrentUser(
            @Valid @RequestBody UpdateUserRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {

        log.info("Updating profile for user: {}", user.getUsername());

        // Remove sensitive fields for regular users
        UpdateUserRequest sanitizedRequest = new UpdateUserRequest(
                request.username(),
                request.email(),
                null, // Don't allow users to change their enabled status
                null  // Don't allow users to change their roles
        );

        UserDTO updated = userService.updateUser(user.getId(), sanitizedRequest);
        log.info("Successfully updated user with ID: {}", updated.id());

        return ResponseEntity.ok(com.moneymapper.budgettracker.dto.ApiResponse.success("Profile updated successfully", updated));
    }

    @Operation(summary = "Update user by ID", description = "Updates a specific user by ID (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "409", description = "Username or email already exists"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<com.moneymapper.budgettracker.dto.ApiResponse<UserDTO>> updateUser(
            @Parameter(description = "User ID") @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {

        log.info("Admin updating user with ID: {}", id);
        UserDTO updated = userService.updateUser(id, request);
        log.info("Successfully updated user with ID: {}", updated.id());

        return ResponseEntity.ok(com.moneymapper.budgettracker.dto.ApiResponse.success("User updated successfully", updated));
    }

    @Operation(summary = "Delete current user", description = "Deletes the authenticated user's account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    @DeleteMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<com.moneymapper.budgettracker.dto.ApiResponse<Void>> deleteCurrentUser(
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {

        log.info("User {} is deleting their account", user.getUsername());
        userService.deleteUser(user.getId());
        log.info("Successfully deleted user with ID: {}", user.getId());

        return ResponseEntity.ok(com.moneymapper.budgettracker.dto.ApiResponse.success("Account deleted successfully"));
    }

    @Operation(summary = "Delete user by ID", description = "Deletes a specific user by ID (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<com.moneymapper.budgettracker.dto.ApiResponse<Void>> deleteUser(
            @Parameter(description = "User ID") @PathVariable Long id) {

        log.info("Admin deleting user with ID: {}", id);
        userService.deleteUser(id);
        log.info("Successfully deleted user with ID: {}", id);

        return ResponseEntity.ok(com.moneymapper.budgettracker.dto.ApiResponse.success("User deleted successfully"));
    }

    @Operation(summary = "Change password", description = "Changes the authenticated user's password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid password data"),
            @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    @PatchMapping("/me/password")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<com.moneymapper.budgettracker.dto.ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {

        log.info("User {} is changing their password", user.getUsername());
        userService.changePassword(user.getId(), request);
        log.info("Successfully changed password for user: {}", user.getUsername());

        return ResponseEntity.ok(com.moneymapper.budgettracker.dto.ApiResponse.success("Password changed successfully"));
    }
}