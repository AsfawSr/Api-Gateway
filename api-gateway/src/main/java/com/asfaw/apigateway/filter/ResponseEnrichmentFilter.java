package com.asfaw.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Custom Post-Filter (Commit 15)
 *
 * CONCEPT: Post-Filter (Response Modification)
 * ─────────────────────────────────────────────
 * A POST filter runs AFTER the downstream service responds.
 * The filter chain works like a stack:
 *
 *   → Pre (filter A) → Pre (filter B) → [downstream service]
 *   ← Post (filter B) ← Post (filter A) ←
 *
 * Use cases for POST filters:
 *   - Add response headers (e.g., CORS, cache-control)
 *   - Log response details (status, timing)
 *   - Transform response body (advanced)
 *   - Add security headers
 */
@Slf4j
@Component
public class ResponseEnrichmentFilter extends AbstractGatewayFilterFactory<ResponseEnrichmentFilter.Config> {

    public ResponseEnrichmentFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // PRE: record start time
            long startTime = System.currentTimeMillis();

            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                // POST: runs AFTER the downstream service responds
                ServerHttpResponse response = exchange.getResponse();
                long duration = System.currentTimeMillis() - startTime;

                // Add custom response headers
                response.getHeaders().add("X-Response-Time-Ms", String.valueOf(duration));
                response.getHeaders().add("X-Served-By", "api-gateway");
                response.getHeaders().add("X-Gateway-Version", "1.0.0");

                if (config.isAddCorsHeaders()) {
                    response.getHeaders().add("Access-Control-Allow-Origin", "*");
                    response.getHeaders().add("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
                    response.getHeaders().add("Access-Control-Allow-Headers", "Authorization,Content-Type");
                }

                log.info("[POST-FILTER ResponseEnrichment] Status={} | Duration={}ms | Path={}",
                        response.getStatusCode(),
                        duration,
                        exchange.getRequest().getURI().getPath());
            }));
        };
    }

    public static class Config {
        private boolean addCorsHeaders = true;

        public boolean isAddCorsHeaders() { return addCorsHeaders; }
        public void setAddCorsHeaders(boolean addCorsHeaders) { this.addCorsHeaders = addCorsHeaders; }
    }
}
