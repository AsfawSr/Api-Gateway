package com.asfaw.userservice.service;

import com.asfaw.userservice.dto.UserRequest;
import com.asfaw.userservice.dto.UserResponse;
import com.asfaw.userservice.entity.User;
import com.asfaw.userservice.repository.UserRepository;
import com.asfaw.userservice.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * User Service
 *
 * CONCEPT: Service Layer Pattern
 * ───────────────────────────────
 * The service layer contains BUSINESS LOGIC only.
 * Controllers handle HTTP; Services handle logic.
 * This separation keeps code clean and testable.
 *
 * @Transactional → wraps the method in a DB transaction.
 * If any exception occurs, the transaction is rolled back.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Transactional
    public UserResponse.UserDto register(UserRequest.RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.ROLE_USER)
                .build();

        User saved = userRepository.save(user);
        log.info("New user registered: {}", saved.getUsername());
        return toDto(saved);
    }

    public UserResponse.AuthResponse login(UserRequest.LoginRequest request) {
        // Spring Security's AuthenticationManager handles credential verification
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

        return UserResponse.AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole().name())
                .expiresIn(jwtUtil.getExpirationMs() / 1000)
                .build();
    }

    @Transactional(readOnly = true)
    public List<UserResponse.UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse.UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return toDto(user);
    }

    @Transactional
    public UserResponse.UserDto updateUser(Long id, UserRequest.UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        if (request.getUsername() != null) user.setUsername(request.getUsername());
        if (request.getEmail() != null) user.setEmail(request.getEmail());

        return toDto(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
        log.info("User deleted: {}", id);
    }

    // ── Helper ────────────────────────────────────────────────────────────
    private UserResponse.UserDto toDto(User user) {
        return UserResponse.UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
