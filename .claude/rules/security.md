---
name: security
description: Security best practices and mandatory checks
---

# Security Rules

## Mandatory Security Checks
1. **No hardcoded secrets** in source code
2. **All API endpoints must have authentication** (except public endpoints)
3. **Input validation** on all request DTOs
4. **SQL injection prevention** (use JPA/QueryDSL, never string concatenation)
5. **XSS prevention** (sanitize user input)

## Secrets Management

### ❌ NEVER Do This
```java
// Hardcoded secrets
String apiKey = "sk_live_abc123";
String dbPassword = "mypassword123";
String jwtSecret = "supersecret";
```

### ✅ Use Environment Variables
```java
@Value("${jwt.secret}")
private String jwtSecret;

@Value("${spring.datasource.password}")
private String dbPassword;
```

```yaml
# application.yml
jwt:
  secret: ${JWT_SECRET}
  
spring:
  datasource:
    password: ${DB_PASSWORD}
```

## Authentication & Authorization

### JWT Token Validation
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtTokenProvider tokenProvider;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);
        
        if (token != null && tokenProvider.validateToken(token)) {
            Authentication auth = tokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        
        filterChain.doFilter(request, response);
    }
}
```

### Security Configuration
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Only for API, enable for web apps
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthenticationFilter(), 
                UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

## Input Validation

### Request DTO Validation
```java
public record UserRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be 2-50 characters")
    String name,
    
    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$",
             message = "Password must be 8+ chars with letters and numbers")
    String password
) {}
```

### Controller Validation
```java
@RestController
public class UserController {
    
    @PostMapping("/api/users")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        // @Valid triggers validation
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(userService.create(request));
    }
}
```

## SQL Injection Prevention

### ✅ Safe: Use JPA/QueryDSL
```java
// JPA Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email); // Safe
}

// QueryDSL
public List<User> findByName(String name) {
    return queryFactory
        .selectFrom(user)
        .where(user.name.eq(name)) // Safe - parameterized
        .fetch();
}
```

### ❌ NEVER: String concatenation
```java
// SQL Injection vulnerability!
String query = "SELECT * FROM users WHERE email = '" + email + "'";
entityManager.createNativeQuery(query).getResultList();
```

## Password Security

### Password Encoding
```java
@Configuration
public class PasswordConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

@Service
public class UserService {
    
    private final PasswordEncoder passwordEncoder;
    
    public void createUser(UserRequest request) {
        String encodedPassword = passwordEncoder.encode(request.password());
        // Save encoded password
    }
}
```

### Password Validation Rules
- Minimum 8 characters
- At least one letter
- At least one number
- Consider special characters for sensitive apps

## CORS Configuration

```java
@Configuration
public class CorsConfig {
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("https://yourdomain.com")); // Don't use "*" in production
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
```

## Rate Limiting (Basic)

```java
@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    
    private final Map<String, List<Long>> requestCounts = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS = 100;
    private static final long TIME_WINDOW = 60000; // 1 minute
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                              HttpServletResponse response,
                              Object handler) throws Exception {
        String clientId = getClientIdentifier(request);
        
        if (isRateLimitExceeded(clientId)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            return false;
        }
        
        return true;
    }
}
```

## Logging Security

### ❌ Don't Log Sensitive Data
```java
// Bad - logs password
log.info("User login attempt: {}", request);

// Bad - logs token
log.info("JWT token: {}", token);
```

### ✅ Log Safely
```java
// Good - only log non-sensitive data
log.info("User login attempt: email={}", request.email());

// Good - mask sensitive data
log.info("Processing payment for user: {}", userId);
```

## Common Vulnerabilities to Avoid

### 1. Mass Assignment
```java
// ❌ Bad - allows setting any field
@PutMapping("/{id}")
public void update(@PathVariable Long id, @RequestBody User user) {
    userRepository.save(user); // Can set id, role, etc.
}

// ✅ Good - use specific DTO
@PutMapping("/{id}")
public void update(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
    User user = userRepository.findById(id).orElseThrow();
    user.setName(request.name());
    user.setEmail(request.email());
    // Only allowed fields can be updated
}
```

### 2. Information Disclosure
```java
// ❌ Bad - exposes internal error
catch (Exception e) {
    return ResponseEntity.status(500).body(e.getMessage());
}

// ✅ Good - generic error message
catch (Exception e) {
    log.error("Error processing request", e);
    return ResponseEntity.status(500).body("Internal server error");
}
```

### 3. Insecure Direct Object Reference (IDOR)
```java
// ❌ Bad - no authorization check
@GetMapping("/{id}")
public UserDTO getUser(@PathVariable Long id) {
    return userQueryService.findById(id); // Anyone can access any user
}

// ✅ Good - verify ownership
@GetMapping("/{id}")
public UserDTO getUser(@PathVariable Long id, @AuthenticationPrincipal UserDetails currentUser) {
    if (!isAuthorized(currentUser, id)) {
        throw new AccessDeniedException("Not authorized");
    }
    return userQueryService.findById(id);
}
```

## Security Checklist Before Deployment
- [ ] No hardcoded secrets
- [ ] All endpoints authenticated (except public)
- [ ] Input validation on all DTOs
- [ ] Passwords encrypted (BCrypt)
- [ ] CORS configured properly
- [ ] HTTPS enforced
- [ ] Rate limiting implemented
- [ ] Error messages don't expose internals
- [ ] Security headers set (CSP, X-Frame-Options, etc.)
- [ ] Dependencies updated (no known vulnerabilities)
