package com.asfaw.apigateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * Rate Limiting Configuration (Commit 17)
 *
 * CONCEPT: Redis-backed Rate Limiting with RequestRateLimiter
 * ────────────────────────────────────────────────────────────
 * Spring Cloud Gateway has built-in rate limiting using Redis.
 * Algorithm: TOKEN BUCKET
 *
 * How Token Bucket Works:
 *   - A "bucket" holds N tokens (replenRate = refillRate per second)
 *   - Each request consumes 1 token
 *   - If bucket is empty → 429 Too Many Requests
 *   - Tokens refill at the replenRate over time
 *
 * Configured in application.yml per route:
 *   filters:
 *     - name: RequestRateLimiter
 *       args:
 *         redis-rate-limiter.replenishRate: 10   ← tokens per second
 *         redis-rate-limiter.burstCapacity: 20   ← max burst size
 *         key-resolver: "#{@ipKeyResolver}"      ← rate limit by IP
 *
 * KEY RESOLVERS:
 *   - By IP (most common)
 *   - By User (from JWT claims)
 *   - By API Key
 */
@Configuration
public class RateLimiterConfig {

    /**
     * Rate limit by client IP address.
     * Each unique IP gets its own token bucket.
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String ip = exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "unknown";
            return Mono.just(ip);
        };
    }

    /**
     * Rate limit by authenticated user (from X-User-Name header set by our JWT filter).
     * Falls back to "anonymous" if no user header.
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String user = exchange.getRequest().getHeaders().getFirst("X-User-Name");
            return Mono.just(user != null ? user : "anonymous");
        };
    }
}
