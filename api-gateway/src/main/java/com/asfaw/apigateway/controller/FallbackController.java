package com.asfaw.apigateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Fallback Controller (Commit 18-19)
 *
 * CONCEPT: Circuit Breaker Fallback
 * ──────────────────────────────────
 * When a downstream service is unavailable or slow, the circuit breaker
 * "trips" and redirects requests to a fallback endpoint.
 *
 * Instead of waiting for timeouts or receiving 503 errors,
 * the client gets a meaningful "service unavailable" response immediately.
 *
 * Circuit Breaker States:
 *   CLOSED   → requests flow normally (circuit is good)
 *   OPEN     → requests are blocked, fallback is used (circuit is tripped)
 *   HALF-OPEN → some requests pass through to test if service recovered
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/user-service")
    public Mono<ResponseEntity<Map<String, Object>>> userServiceFallback() {
        return Mono.just(ResponseEntity.status(503).body(Map.of(
                "service", "user-service",
                "status", "UNAVAILABLE",
                "message", "User Service is currently unavailable. Please try again later.",
                "timestamp", LocalDateTime.now().toString(),
                "fallback", true
        )));
    }

    @GetMapping("/product-service")
    public Mono<ResponseEntity<Map<String, Object>>> productServiceFallback() {
        return Mono.just(ResponseEntity.status(503).body(Map.of(
                "service", "product-service",
                "status", "UNAVAILABLE",
                "message", "Product Service is currently unavailable. Please try again later.",
                "timestamp", LocalDateTime.now().toString(),
                "fallback", true
        )));
    }

    @GetMapping("/order-service")
    public Mono<ResponseEntity<Map<String, Object>>> orderServiceFallback() {
        return Mono.just(ResponseEntity.status(503).body(Map.of(
                "service", "order-service",
                "status", "UNAVAILABLE",
                "message", "Order Service is currently unavailable. Please try again later.",
                "timestamp", LocalDateTime.now().toString(),
                "fallback", true
        )));
    }
}
