package com.asfaw.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global Logging Filter (Commit 16)
 *
 * CONCEPT: GlobalFilter vs GatewayFilter
 * ─────────────────────────────────────────────────────────────
 * GlobalFilter → applies to ALL routes automatically.
 * GatewayFilter → applies only to routes that explicitly declare it.
 *
 * This filter logs every request and response that passes through the gateway.
 * It acts as the "access log" of the gateway – extremely useful for debugging.
 *
 * REACTIVE PATTERN:
 * ──────────────────
 * We cannot use ThreadLocal or standard blocking code here.
 * Everything is reactive: Mono/Flux + .then() / .doOnNext() / .doFinally()
 *
 * The .then(Mono.fromRunnable(...)) pattern is how we execute
 * POST logic (after downstream responds) in reactive code.
 */
@Slf4j
@Component
public class GlobalLoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // ── PRE: Log incoming request ─────────────────────────────────────
        log.info("╔══════════════════════════════════════════════════════════════");
        log.info("║ [GATEWAY REQUEST]");
        log.info("║  Method  : {}", request.getMethod());
        log.info("║  URI     : {}", request.getURI());
        log.info("║  Headers : {}", request.getHeaders().toSingleValueMap());
        log.info("║  RemoteIP: {}", request.getRemoteAddress());

        long startTime = System.currentTimeMillis();

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            // ── POST: Log outgoing response ───────────────────────────────
            ServerHttpResponse response = exchange.getResponse();
            long duration = System.currentTimeMillis() - startTime;

            log.info("║ [GATEWAY RESPONSE]");
            log.info("║  Status  : {}", response.getStatusCode());
            log.info("║  Duration: {}ms", duration);
            log.info("╚══════════════════════════════════════════════════════════════");
        }));
    }

    @Override
    public int getOrder() {
        // Run AFTER JWT filter (-100) but before routing
        return -50;
    }
}
