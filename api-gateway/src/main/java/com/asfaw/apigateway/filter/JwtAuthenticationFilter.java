package com.asfaw.apigateway.filter;

import com.asfaw.apigateway.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * JWT Authentication Global Filter (Commit 13)
 *
 * CONCEPT: Global Filter for Authentication
 * ──────────────────────────────────────────────
 * This filter runs BEFORE every request hits a downstream service.
 * It intercepts, validates JWT, and either:
 *   - Lets the request through (adding user info as headers)
 *   - Returns 401 Unauthorized immediately
 *
 * WHY AT THE GATEWAY?
 * ─────────────────────
 * Authentication at the gateway means downstream services don't need
 * to repeat auth logic. The gateway is the "trust boundary".
 * Once inside, services can trust the X-User-* headers set by the gateway.
 *
 * IMPORTANT: GlobalFilter applies to ALL routes.
 * Use GatewayFilter (per-route) for selective auth.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    // Public routes that do NOT need a JWT token
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/register",
            "/api/auth/login",
            "/actuator"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        log.debug("Gateway processing request: {} {}", request.getMethod(), path);

        // Skip JWT check for public paths
        if (isPublicPath(path)) {
            log.debug("Public path – skipping JWT check: {}", path);
            return chain.filter(exchange);
        }

        // Extract token from Authorization header
        String token = extractToken(request);
        if (!StringUtils.hasText(token)) {
            log.warn("No JWT token for protected path: {}", path);
            return sendUnauthorized(exchange, "Missing or invalid Authorization header");
        }

        // Validate token
        if (!jwtUtil.validateToken(token)) {
            log.warn("Invalid JWT token for path: {}", path);
            return sendUnauthorized(exchange, "Invalid or expired JWT token");
        }

        // ✅ Token is valid – add user info to downstream request headers
        String username = jwtUtil.extractUsername(token);
        String role     = jwtUtil.extractRole(token);

        log.debug("Authenticated user: {} [{}] → {}", username, role, path);

        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-User-Name", username)
                .header("X-User-Role", role)
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        // Run early – before routing filters
        return -100;
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private String extractToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private Mono<Void> sendUnauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        String body = "{\"error\": \"Unauthorized\", \"message\": \"" + message + "\"}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
