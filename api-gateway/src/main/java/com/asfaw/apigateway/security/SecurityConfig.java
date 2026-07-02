package com.asfaw.apigateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Gateway Security Configuration
 *
 * CONCEPT: Reactive Security (WebFlux)
 * ──────────────────────────────────────
 * In a reactive gateway, we use ServerHttpSecurity (not HttpSecurity).
 * We disable Spring Security's default login page and CSRF protection
 * because:
 *   1. JWT handles authentication, not form login
 *   2. The gateway is stateless (no server-side session)
 *
 * The actual JWT validation is done by our JwtAuthenticationFilter (GlobalFilter).
 * Spring Security here just disables defaults that would interfere.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                // Let the JWT GlobalFilter handle auth – permit all at Spring Security level
                .authorizeExchange(exchanges -> exchanges
                        .anyExchange().permitAll()
                )
                .build();
    }
}
