package com.asfaw.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTOs
 *
 * CONCEPT: DTO Pattern (Data Transfer Objects)
 * ─────────────────────────────────────────────
 * We never expose entities directly in APIs.
 * DTOs carry only the data needed for that specific operation.
 *
 * @NotBlank  → field cannot be null or empty string
 * @Size      → length constraints
 * @Email     → must be a valid email format
 */
public class UserRequest {

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
        private String username;

        @NotBlank(message = "Email is required")
        @Email(message = "Must be a valid email")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;
    }

    @Data
    public static class LoginRequest {
        @NotBlank(message = "Username is required")
        private String username;

        @NotBlank(message = "Password is required")
        private String password;
    }

    @Data
    public static class UpdateUserRequest {
        @Size(min = 3, max = 50)
        private String username;

        @Email
        private String email;
    }
}
