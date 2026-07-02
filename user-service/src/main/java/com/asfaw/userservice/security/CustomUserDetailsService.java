package com.asfaw.userservice.security;

import com.asfaw.userservice.entity.User;
import com.asfaw.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Custom UserDetailsService
 *
 * CONCEPT: Spring Security – UserDetailsService
 * ──────────────────────────────────────────────
 * Spring Security needs to know how to load a user by username.
 * We implement UserDetailsService and override loadUserByUsername()
 * to query our database.
 *
 * Spring Security then uses the returned UserDetails object to:
 *   - verify the password during login
 *   - build the SecurityContext (who is currently logged in)
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority(user.getRole().name()))
        );
    }
}
