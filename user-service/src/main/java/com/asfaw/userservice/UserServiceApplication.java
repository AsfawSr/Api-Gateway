package com.asfaw.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * User Service Application
 *
 * CONCEPT: Microservice with Eureka Client
 * ─────────────────────────────────────────
 * @EnableDiscoveryClient → registers this service with Eureka on startup.
 * The service name (spring.application.name) is how other services will discover it.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
