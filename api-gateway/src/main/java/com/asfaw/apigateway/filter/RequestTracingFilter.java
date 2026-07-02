package com.asfaw.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Custom Pre-Filter (Commit 14)
 *
 * CONCEPT: Pre-Filter (GatewayFilterFactory)
 * ───────────────────────────────────────────
 * A PRE filter runs BEFORE the request is sent to the downstream service.
 * Use cases:
 *   - Add/modify request headers
 *   - Log incoming request details
 *   - Validate custom headers
 *   - Add a correlation/request ID for tracing
 *
 * GatewayFilterFactory → creates per-ROUTE filters (not global).
 * Applied in application.yml under: filters: - RequestLogging
 *
 * Inner Config class holds configuration from YAML:
 *   filters:
 *     - name: RequestTracing
 *       args:
 *         logBody: true
 */
@Slf4j
@Component
public class RequestTracingFilter extends AbstractGatewayFilterFactory<RequestTracingFilter.Config> {

    public RequestTracingFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // ── PRE logic (before forwarding to downstream) ──────────────────
            String requestId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String timestamp = LocalDateTime.now().toString();

            log.info("[PRE-FILTER] RequestID={} | Method={} | Path={} | Time={}",
                    requestId,
                    exchange.getRequest().getMethod(),
                    exchange.getRequest().getURI().getPath(),
                    timestamp);

            // Add tracing headers to the downstream request
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-Request-ID", requestId)
                    .header("X-Request-Timestamp", timestamp)
                    .header("X-Gateway-Source", "api-gateway")
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build())
                    .then(java.reactor.core.publisher.Mono.fromRunnable(() -> {
                        // ── POST logic (after getting response from downstream) ─────────
                        log.info("[POST-FILTER] RequestID={} | Status={}",
                                requestId,
                                exchange.getResponse().getStatusCode());
                    }));
        };
    }

    /**
     * Configuration class – properties can be set per route in YAML.
     */
    public static class Config {
        private boolean logBody = false;

        public boolean isLogBody() { return logBody; }
        public void setLogBody(boolean logBody) { this.logBody = logBody; }
    }
}
