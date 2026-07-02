package com.asfaw.userservice.repository;

import com.asfaw.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * User Repository
 *
 * CONCEPT: Spring Data JPA Repository
 * ─────────────────────────────────────
 * Extending JpaRepository gives us free CRUD methods:
 *   - save(), findById(), findAll(), deleteById(), etc.
 * We only need to define CUSTOM queries here.
 *
 * Spring derives the SQL from method names automatically:
 *   findByUsername → SELECT * FROM users WHERE username = ?
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
