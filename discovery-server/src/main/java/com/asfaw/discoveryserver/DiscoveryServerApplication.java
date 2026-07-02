package com.asfaw.discoveryserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka Discovery Server
 *
 * CONCEPT: Service Discovery
 * ─────────────────────────
 * In a microservices architecture, services need to find each other dynamically
 * without hardcoded IPs/ports. Eureka solves this:
 *
 *   1. Services REGISTER themselves with Eureka on startup (Eureka Client)
 *   2. Services DISCOVER other services by querying Eureka (by name, not IP)
 *   3. Eureka tracks which instances are healthy via heartbeats
 *
 * @EnableEurekaServer  → turns this app into a Eureka registry server
 */
@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServerApplication.class, args);
    }
}
