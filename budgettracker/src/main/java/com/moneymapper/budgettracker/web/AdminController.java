package com.moneymapper.budgettracker.web;

import com.moneymapper.budgettracker.domain.User;
import com.moneymapper.budgettracker.dto.ApiResponse;
import com.moneymapper.budgettracker.dto.UserResponse;
import com.moneymapper.budgettracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<User> users = userService.findAllUsers();
        List<UserResponse> userResponses = users.stream()
                .map(userService::toUserResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(userResponses));
    }

    @GetMapping("/users/search")
    public ResponseEntity<ApiResponse<List<UserResponse>>> searchUsers(@RequestParam String query) {
        List<User> users = userService.searchUsers(query);
        List<UserResponse> userResponses = users.stream()
                .map(userService::toUserResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(userResponses));
    }

    @GetMapping("/users/{username}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByUsername(@PathVariable String username) {
        return userService.findByUsername(username)
                .map(user -> ResponseEntity.ok(ApiResponse.success(userService.toUserResponse(user))))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/users/{username}/roles/{role}")
    public ResponseEntity<ApiResponse<UserResponse>> addRoleToUser(
            @PathVariable String username,
            @PathVariable String role) {
        try {
            User user = userService.addRoleToUser(username, role);
            return ResponseEntity.ok(ApiResponse.success("Role added successfully",
                    userService.toUserResponse(user)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/users/{username}/roles/{role}")
    public ResponseEntity<ApiResponse<UserResponse>> removeRoleFromUser(
            @PathVariable String username,
            @PathVariable String role) {
        try {
            User user = userService.removeRoleFromUser(username, role);
            return ResponseEntity.ok(ApiResponse.success("Role removed successfully",
                    userService.toUserResponse(user)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/users/{username}/enable")
    public ResponseEntity<ApiResponse<UserResponse>> enableUser(@PathVariable String username) {
        try {
            User user = userService.setUserEnabled(username, true);
            return ResponseEntity.ok(ApiResponse.success("User enabled successfully",
                    userService.toUserResponse(user)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/users/{username}/disable")
    public ResponseEntity<ApiResponse<UserResponse>> disableUser(@PathVariable String username) {
        try {
            User user = userService.setUserEnabled(username, false);
            return ResponseEntity.ok(ApiResponse.success("User disabled successfully",
                    userService.toUserResponse(user)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/users/{username}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable String username) {
        try {
            userService.deleteUserByUsername(username);
            return ResponseEntity.ok(ApiResponse.success("User deleted successfully", username));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}