package com.asfaw.apigateway.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT Utility for API Gateway
 *
 * CONCEPT: JWT Validation at the Gateway (Commit 13)
 * ────────────────────────────────────────────────────
 * The gateway VALIDATES JWT tokens (doesn't create them).
 * Token creation is the responsibility of the user-service.
 *
 * Gateway validates:
 *   1. Signature → ensures the token was issued by our user-service
 *   2. Expiry    → ensures the token hasn't expired
 *   3. Claims    → reads role to apply authorization
 *
 * The same secret key MUST be shared between user-service and api-gateway.
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("JWT validation failed: {}", e.getMessage());
        }
        return false;
    }

    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
