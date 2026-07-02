package com.asfaw.userservice.controller;

import com.asfaw.userservice.dto.UserRequest;
import com.asfaw.userservice.dto.UserResponse;
import com.asfaw.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 * Handles register and login – these are public (no auth required).
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * POST /api/auth/register → register a new user
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse.ApiResponse<UserResponse.UserDto>> register(
            @Valid @RequestBody UserRequest.RegisterRequest request) {
        UserResponse.UserDto user = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UserResponse.ApiResponse.success("User registered successfully", user));
    }

    /**
     * POST /api/auth/login → authenticate and get JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<UserResponse.ApiResponse<UserResponse.AuthResponse>> login(
            @Valid @RequestBody UserRequest.LoginRequest request) {
        UserResponse.AuthResponse authResponse = userService.login(request);
        return ResponseEntity.ok(UserResponse.ApiResponse.success("Login successful", authResponse));
    }
}
