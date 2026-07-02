package com.asfaw.userservice.controller;

import com.asfaw.userservice.dto.UserRequest;
import com.asfaw.userservice.dto.UserResponse;
import com.asfaw.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User REST Controller
 *
 * CONCEPT: REST API Design
 * ─────────────────────────
 * REST uses HTTP methods to indicate intent:
 *   GET    → read data
 *   POST   → create new data
 *   PUT    → update (full replacement)
 *   PATCH  → partial update
 *   DELETE → remove data
 *
 * @PreAuthorize → method-level security; checked AFTER authentication.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * GET /api/users → list all users (ADMIN only)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse.ApiResponse<List<UserResponse.UserDto>>> getAllUsers() {
        List<UserResponse.UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(UserResponse.ApiResponse.success("Users retrieved", users));
    }

    /**
     * GET /api/users/{id} → get user by id
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse.ApiResponse<UserResponse.UserDto>> getUserById(@PathVariable Long id) {
        UserResponse.UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(UserResponse.ApiResponse.success("User retrieved", user));
    }

    /**
     * PUT /api/users/{id} → update user
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse.ApiResponse<UserResponse.UserDto>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest.UpdateUserRequest request) {
        UserResponse.UserDto updated = userService.updateUser(id, request);
        return ResponseEntity.ok(UserResponse.ApiResponse.success("User updated", updated));
    }

    /**
     * DELETE /api/users/{id} → delete user (ADMIN only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse.ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(UserResponse.ApiResponse.success("User deleted", null));
    }
}
