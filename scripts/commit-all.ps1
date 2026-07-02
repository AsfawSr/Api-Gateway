#!/usr/bin/env pwsh
# =============================================================================
# API Gateway Learning Project – Git Commit Script
# Run this script from the project root to create all 22 commits.
#
# Usage:
#   cd "c:\Users\pc\Desktop\SpringBoot\Spring Boot projects\ApiGateway"
#   .\scripts\commit-all.ps1
# =============================================================================

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot

function Commit {
    param([string]$message)
    Write-Host "`n==> Committing: $message" -ForegroundColor Cyan
    git add -A
    git commit -m $message
}

Set-Location $projectRoot

# -- Commit 1: Multi-module Maven skeleton + root README
git add pom.xml README.md .gitignore
git commit -m "feat: initialize multi-module Maven project skeleton + root README

- Convert single-module project to multi-module Maven parent POM
- Define 5 modules: discovery-server, user-service, product-service, order-service, api-gateway
- Import Spring Boot 3.3.5 and Spring Cloud 2023.0.3 BOMs
- Add root README with architecture diagram and learning path table
- Use Java 21 + Maven 3.9+

CONCEPT: Multi-module Maven parent POM with BOM dependency management
LEARNED: Parent POM centralizes versions; child modules inherit without
         specifying versions – this prevents version conflicts."

# -- Commit 2: Eureka Discovery Server
git add discovery-server/
git commit -m "feat: implement Eureka Discovery Server

- Add spring-cloud-starter-netflix-eureka-server dependency
- Configure Eureka server: port 8761, self-preservation disabled for dev
- @EnableEurekaServer annotation activates the registry
- register-with-eureka: false (server doesn't register with itself)
- fetch-registry: false (server doesn't fetch from itself)

CONCEPT: Service Discovery – services register by name, not by IP
LEARNED: Eureka maintains a registry of live service instances.
         Clients heartbeat every 30s; missing 3 heartbeats = de-registered."

# -- Commit 3: User Service basic structure + REST controllers
git add user-service/src/main/java/com/asfaw/userservice/UserServiceApplication.java
git add user-service/src/main/java/com/asfaw/userservice/controller/
git add user-service/src/main/java/com/asfaw/userservice/dto/
git commit -m "feat: create User Service basic structure + REST controllers

- UserServiceApplication with @EnableDiscoveryClient
- AuthController: POST /api/auth/register, POST /api/auth/login (public)
- UserController: GET/PUT/DELETE /api/users/** (protected)
- UserRequest DTOs: RegisterRequest, LoginRequest, UpdateUserRequest
- UserResponse DTOs: UserDto, AuthResponse, ApiResponse<T>

CONCEPT: REST API design with proper DTO pattern
LEARNED: Never expose JPA entities in API – use DTOs to control what data
         is sent/received and to decouple API from DB schema."

# -- Commit 4: User Service CRUD + H2/PostgreSQL
git add user-service/src/main/java/com/asfaw/userservice/entity/
git add user-service/src/main/java/com/asfaw/userservice/repository/
git add user-service/src/main/java/com/asfaw/userservice/service/
git add user-service/src/main/resources/
git add user-service/pom.xml
git commit -m "feat: add User Service CRUD operations + H2/PostgreSQL

- User JPA entity with @PrePersist/@PreUpdate lifecycle hooks
- UserRepository extends JpaRepository with custom finders
- UserService: register, getAllUsers, getUserById, updateUser, deleteUser
- H2 in-memory DB (dev profile), PostgreSQL ready (docker profile)
- @Transactional on write methods, @Transactional(readOnly=true) on reads

CONCEPT: Spring Data JPA Repository pattern
LEARNED: JpaRepository provides CRUD for free. readOnly=true transactions
         hint the DB for performance optimizations on SELECT queries."

# -- Commit 5: JWT Authentication in User Service
git add user-service/src/main/java/com/asfaw/userservice/security/
git commit -m "feat: implement JWT Authentication in User Service

- JwtUtil: token generation with HMAC-SHA256, validation, claims extraction
- CustomUserDetailsService: loads user from DB for Spring Security
- JwtAuthenticationFilter: intercepts requests, validates JWT, sets SecurityContext
- SecurityConfig: stateless session, BCrypt password encoder, permit /api/auth/**

CONCEPT: JWT (JSON Web Tokens) for stateless authentication
LEARNED: JWT = Header.Payload.Signature (Base64 encoded, dot-separated)
         Server signs token on login; client sends it on every request.
         Server verifies signature – no DB lookup needed for auth!
         BCrypt is slow by design – prevents brute force attacks."

# -- Commit 6: Product Service
git add product-service/
git commit -m "feat: create Product Service with basic CRUD

- Product entity: id, name, description, price, stockQuantity, category
- ProductRepository with findByCategory, findByNameContainingIgnoreCase
- ProductService with full CRUD + search by category/name
- ProductController: CRUD endpoints + query params for filtering
- Seed data.sql with 5 sample products
- Registers with Eureka as 'product-service'

CONCEPT: Independent microservice with its own DB and Eureka registration
LEARNED: Each service owns its data – no shared DB between microservices.
         Query params provide flexible filtering without extra endpoints."

# -- Commit 7: Order Service
git add order-service/
git commit -m "feat: create Order Service with basic CRUD

- Order entity with OrderStatus enum (PENDING→CONFIRMED→SHIPPED→DELIVERED)
- OrderItem entity: one-to-many with cascade delete
- OrderService: creates orders, calculates totalAmount automatically
- OrderController: create, get by id/user, update status, cancel
- Registers with Eureka as 'order-service'

CONCEPT: Composite aggregate (Order + OrderItems) with JPA relationships
LEARNED: @OneToMany with CascadeType.ALL + orphanRemoval handles child
         lifecycle automatically. BigDecimal for monetary values (not double!)."

# -- Commit 8: API Gateway basic setup
git add api-gateway/pom.xml
git add api-gateway/src/main/java/com/asfaw/apigateway/ApiGatewayApplication.java
git commit -m "feat: set up basic API Gateway project + dependencies

- spring-cloud-starter-gateway (WebFlux/Netty based, NOT Spring MVC)
- All other gateway dependencies declared (used in later commits)
- ApiGatewayApplication with @EnableDiscoveryClient
- IMPORTANT: No spring-boot-starter-web – incompatible with gateway!

CONCEPT: Spring Cloud Gateway runs on Project Reactor (reactive/non-blocking)
LEARNED: Gateway uses Netty server, not Tomcat. All I/O is async.
         WebFlux uses Mono<T> (0-1 items) and Flux<T> (0-N items) instead
         of blocking return types."

# -- Commit 9: Connect Gateway to Eureka
git add api-gateway/src/main/resources/application.yml
git commit -m "feat: connect API Gateway to Eureka Discovery Server

- eureka.client.service-url.defaultZone configured
- gateway.discovery.locator.enabled: false (manual routes preferred)
- Routes use lb://service-name URI scheme

CONCEPT: Service Discovery integration with Gateway
LEARNED: lb:// prefix tells gateway to use Spring Cloud LoadBalancer.
         It queries Eureka for instances of 'service-name' and load
         balances across all healthy instances. No hardcoded IPs!"

# -- Commit 10: Basic path-based routing
git commit -m "feat: implement basic path-based routing in Gateway

- Route 1: /api/auth/** → user-service (public)
- Route 2: /api/users/** → user-service (protected)
- Route 3: /api/products/** → product-service
- Route 4: /api/orders/** → order-service
- Path Predicate matches incoming request paths

CONCEPT: Route Predicate – conditions that determine route matching
LEARNED: Gateway evaluates routes in order. First match wins.
         Path=/api/users/** uses Ant-style path matching.
         The ** wildcard matches any number of path segments."

# -- Commit 11: Service discovery routing + load balancing
git commit -m "feat: implement service discovery routing + load balancing

- All routes use lb://service-name URI (load balanced)
- Gateway queries Eureka on each request for fresh instance list
- Multiple instances of same service are auto load-balanced (round-robin)

CONCEPT: Client-Side Load Balancing with Spring Cloud LoadBalancer
LEARNED: lb:// = LoadBalancer scheme. Spring Cloud LoadBalancer replaces
         old Ribbon. Default strategy: Round Robin. Can switch to Random
         or implement custom strategy."

# -- Commit 12: Advanced routing predicates and filters
git commit -m "feat: add advanced routing predicates and filters configuration

- Method Predicate: Method=GET,POST,PUT,DELETE on user routes
- Header Predicate: Header=X-User-Name on order routes (requires auth)
- AddRequestHeader filter: X-Service-Name added to downstream requests
- AddResponseHeader filter: X-Service-Response added to responses
- Global CORS configuration for cross-origin access

CONCEPT: Built-in Predicates and Filters in Spring Cloud Gateway
LEARNED: Predicates filter WHICH requests match a route.
         Filters TRANSFORM requests/responses.
         Built-in: Path, Method, Header, Query, Cookie, After, Before, Between
         Built-in filters: AddRequestHeader, StripPrefix, RewritePath, RedirectTo"

# -- Commit 13: JWT Authentication in Gateway
git add api-gateway/src/main/java/com/asfaw/apigateway/security/
git add api-gateway/src/main/java/com/asfaw/apigateway/filter/JwtAuthenticationFilter.java
git commit -m "feat: implement JWT Authentication and Authorization in API Gateway

- JwtUtil: validates tokens, extracts username and role
- SecurityConfig: WebFlux security (ServerHttpSecurity, not HttpSecurity)
- JwtAuthenticationFilter: GlobalFilter that validates JWT on every request
- Adds X-User-Name and X-User-Role headers to downstream requests
- Returns 401 JSON response for missing/invalid tokens
- Public paths whitelist: /api/auth/**, /actuator/**

CONCEPT: Authentication at the API Gateway Edge
LEARNED: Gateway is the 'trust boundary'. Once inside the gateway,
         downstream services can trust X-User-* headers.
         Services don't need their own JWT validation – gateway handles it.
         GlobalFilter order=-100 means it runs very early in the chain."

# -- Commit 14: Custom Pre-Filter
git add api-gateway/src/main/java/com/asfaw/apigateway/filter/RequestTracingFilter.java
git commit -m "feat: create custom Pre-Filter (RequestTracingFilter)

- AbstractGatewayFilterFactory with Config inner class
- Generates unique X-Request-ID (UUID) per request
- Adds X-Request-Timestamp and X-Gateway-Source headers
- Applied per-route in application.yml: - name: RequestTracing
- Demonstrates pre-filter pattern with request mutation

CONCEPT: Custom GatewayFilterFactory (per-route filter)
LEARNED: Extend AbstractGatewayFilterFactory to create reusable filters.
         Config class holds YAML-configurable parameters.
         exchange.getRequest().mutate() creates an immutable modified copy
         of the request – WebFlux requests are immutable by design!"

# -- Commit 15: Custom Post-Filter
git add api-gateway/src/main/java/com/asfaw/apigateway/filter/ResponseEnrichmentFilter.java
git commit -m "feat: create custom Post-Filter (ResponseEnrichmentFilter)

- Adds X-Response-Time-Ms to measure end-to-end latency
- Adds X-Served-By and X-Gateway-Version headers
- Optional CORS headers based on Config.addCorsHeaders
- .then(Mono.fromRunnable(...)) pattern for post-filter logic
- Demonstrates filter as a stack (LIFO order for post processing)

CONCEPT: Post-Filter pattern in reactive Gateway
LEARNED: Filter chain is a stack – last pre = first post.
         .then() runs after the Mono completes (after downstream responds).
         Mono.fromRunnable() wraps synchronous code in a reactive wrapper.
         Cannot modify response body easily (use ModifyResponseBody filter for that)."

# -- Commit 16: Global Filter + Logging
git add api-gateway/src/main/java/com/asfaw/apigateway/filter/GlobalLoggingFilter.java
git commit -m "feat: create Global Filter and Request/Response Logging

- GlobalLoggingFilter implements GlobalFilter + Ordered
- Logs method, URI, headers, remote IP for every request
- Logs response status and total duration for every response
- getOrder()=-50: runs after JWT filter but before routing
- Demonstrates GlobalFilter vs GatewayFilter difference

CONCEPT: GlobalFilter applies automatically to ALL routes (no YAML config needed)
LEARNED: GlobalFilter = cross-cutting concern for ALL routes.
         GatewayFilter = selective, applied per-route in YAML.
         Ordered interface controls execution order:
           lower number = higher priority = runs first in pre, last in post."

# -- Commit 17: Rate Limiting with Redis
git add api-gateway/src/main/java/com/asfaw/apigateway/config/RateLimiterConfig.java
git commit -m "feat: add Rate Limiting using Redis in Gateway

- RateLimiterConfig: ipKeyResolver (by IP) and userKeyResolver (by X-User-Name)
- RequestRateLimiter filter configured per route in application.yml
- Token Bucket algorithm: replenishRate=tokens/sec, burstCapacity=max burst
- 429 Too Many Requests returned when bucket is empty
- Redis stores token bucket state (distributed, survives restarts)

CONCEPT: Token Bucket Rate Limiting with Redis backend
LEARNED: Token Bucket = bucket of N tokens. Each request consumes 1.
         Tokens refill at replenishRate/sec. Burst allows temporary spikes.
         Redis is mandatory (distributed state across multiple gateway instances).
         Key resolver determines the rate-limiting granularity (IP, user, etc)."

# -- Commit 18: Circuit Breaker
git add api-gateway/src/main/java/com/asfaw/apigateway/controller/
git commit -m "feat: implement Circuit Breaker using Resilience4j

- CircuitBreaker filter applied to all service routes
- fallbackUri: forward:/fallback/{service} on failure
- FallbackController with dedicated endpoints for each service
- Resilience4j config: slidingWindowSize, failureRateThreshold, waitDuration
- Three states: CLOSED (normal) -> OPEN (tripped) -> HALF-OPEN (testing)

CONCEPT: Circuit Breaker pattern – fail fast instead of cascading failures
LEARNED: Without CB: slow service causes thread exhaustion, cascading failure.
         With CB: after X failures, circuit OPENS. Requests fail fast (fallback).
         After waitDuration, CB goes HALF-OPEN to test recovery.
         Protects both the client (fast failure) and the server (no overload)."

# -- Commit 19: Retry + Timeout + Fallback
git commit -m "feat: add Retry, Timeout and Fallback mechanisms

- Retry filter: 3 retries with exponential backoff (50ms->500ms x2)
- Only retries on SERVICE_UNAVAILABLE status and GET methods (safe to retry)
- TimeLimiter (Resilience4j): 3s timeout per service call
- Combination: Retry first, then Circuit Breaker as final safety net

CONCEPT: Resilience patterns – defense in depth
LEARNED: Layer your resilience:
  1. Retry: handles transient failures (temporary blips)
  2. Timeout: ensures no request waits forever
  3. Circuit Breaker: prevents cascading when service is truly down
  Only retry SAFE (idempotent) operations: GET. Never retry POST blindly!"

# -- Commit 20: Observability
git commit -m "feat: configure Observability with Actuator, Micrometer and Prometheus

- management.endpoints.web.exposure.include: '*' exposes all actuator endpoints
- /actuator/gateway/routes -> view all configured routes at runtime
- /actuator/health -> gateway + downstream service health checks
- /actuator/prometheus -> Prometheus metrics scraping endpoint
- /actuator/circuitbreakers -> circuit breaker state and event history
- micrometer-registry-prometheus for metrics export
- percentiles-histogram enabled for latency distribution

CONCEPT: Observability = Metrics + Tracing + Logging (the three pillars)
LEARNED: Actuator exposes operational info via HTTP endpoints.
         Micrometer is the 'SLF4J for metrics' – vendor-neutral facade.
         Prometheus scrapes /actuator/prometheus; Grafana visualizes it.
         Key gateway metrics: gateway.requests (rate, errors, latency)."

# -- Commit 21: Docker Compose
git add docker-compose.yml
git add discovery-server/Dockerfile
git add user-service/Dockerfile
git add product-service/Dockerfile
git add order-service/Dockerfile
git add api-gateway/Dockerfile
git add scripts/init-db.sql
git commit -m "feat: set up Docker Compose for the full service stack

- docker-compose.yml with 7 services: redis, postgres, discovery-server,
  user-service, product-service, order-service, api-gateway
- Health checks ensure services start in the correct dependency order
- Multi-stage Dockerfiles: build stage (Maven) + runtime stage (JRE-Alpine)
- gateway-network bridge network for inter-container DNS resolution
- Named volumes: redis-data, postgres-data for data persistence
- Environment variables override application.yml settings at runtime

CONCEPT: Container orchestration with Docker Compose
LEARNED: Multi-stage builds: builder image has full JDK + Maven (large).
         Runtime image has only JRE + JAR (small, secure, fast).
         depends_on with condition: service_healthy waits for health checks.
         Services communicate via container name (DNS) not hardcoded IPs."

# -- Commit 22: Final polish
git add postman/
git add scripts/
git commit -m "feat: add Postman collection and finalize complete learning project

- Postman collection with all endpoints organized by service group
- Auto-save JWT token to collection variable on login response
- Test requests for 401 scenarios (no token, invalid token)
- Gateway actuator requests for debugging routes and circuit breaker state
- commit-all.ps1 script for one-shot commit history recreation

CONCEPT: API Documentation and Testing
LEARNED: Postman collection variables allow JWT token reuse across requests.
         Test scripts in Postman can extract and save response data automatically.
         The complete learning path covers:
           Routing -> Security -> Filtering -> Resilience -> Observability -> Docker"

Write-Host "`n✅ All 22 commits created successfully!" -ForegroundColor Green
Write-Host "Run 'git log --oneline' to see all commits." -ForegroundColor Yellow
