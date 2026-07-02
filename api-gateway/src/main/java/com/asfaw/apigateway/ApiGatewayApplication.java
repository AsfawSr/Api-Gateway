package com.asfaw.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API Gateway Application
 *
 * CONCEPT: The Gateway Entry Point
 * ─────────────────────────────────
 * This is a REACTIVE application (WebFlux / Project Reactor).
 * Unlike traditional Spring MVC apps, everything here is NON-BLOCKING.
 *
 * Key difference from other services:
 *   - Uses Netty server (not Tomcat)
 *   - Uses WebFlux (Mono/Flux) not Servlet API
 *   - All filters work with ServerWebExchange, not HttpServletRequest
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
