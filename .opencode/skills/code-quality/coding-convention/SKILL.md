---
name: coding-convention
description: Java/Spring Boot coding standards including Google Java Style Guide, package structure, DTO naming, Controller/Service/Repository patterns, and error handling conventions. Load this skill before writing any code.
---

# Coding Convention

## Google Java Style Guide
**Follow Google Java Style Guide**: https://google.github.io/styleguide/javaguide.html

Key points:
- **Indentation**: 2 spaces (no tabs)
- **Column limit**: 100 characters
- **Braces**: K&R style (opening brace on same line)
- **Naming**:
  - Classes: `UpperCamelCase`
  - Methods/Variables: `lowerCamelCase`
  - Constants: `UPPER_SNAKE_CASE`
- **Import order**: Static imports first, then regular imports, alphabetically sorted

## Java Standards
- Java 21 features preferred
- Use `final` for immutability when possible
- Max file length: 300 lines
- Use Lombok to reduce boilerplate (`@Getter`, `@Builder`, etc.)
- Use records for DTOs when appropriate

---

## NO SETTERS in Domain Entities

**Setter methods are prohibited in domain entities.** Use business methods instead.

### BAD - Using Setters
```java
@Entity
public class User {
    @Setter // NEVER use @Setter
    private String nickname;
}
```

### GOOD - Using Business Methods
```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    private String nickname;
    private UserStatus status;
    
    // Business method with clear intent
    public void updateProfile(String nickname) {
        this.nickname = nickname;
    }
    
    // State transition with validation
    public void activate() {
        if (this.status != UserStatus.PENDING) {
            throw new IllegalStateException("Cannot activate");
        }
        this.status = UserStatus.ACTIVE;
    }
}
```

---

## Package Structure (Per Module)

```
com.wit.<module>/
├── api/
│   └── <Module>Controller.java
├── application/
│   ├── <Module>Service.java          # interface
│   ├── <Module>ServiceImpl.java      # implementation
│   ├── <Module>QueryService.java     # interface
│   └── <Module>QueryServiceImpl.java # implementation
├── domain/
│   └── <Entity>.java
├── repository/
│   ├── <Entity>Repository.java
│   └── <Entity>QueryRepository.java
├── exception/
│   └── <Module>ErrorCode.java
├── event/
│   └── <Entity>CreatedEvent.java
├── dto/
│   ├── request/
│   │   └── Create<Entity>Request.java
│   ├── response/
│   │   └── <Entity>Response.java
│   └── <Entity><Purpose>Dto.java
```

---

## Spring Boot Patterns

### Dependency Injection
```java
// Constructor injection (preferred)
@Service
public class UserService {
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}

// Field injection (AVOID)
@Service
public class UserService {
    @Autowired // NEVER
    private UserRepository userRepository;
}
```

### Layer Responsibilities
- **Controller**: HTTP request/response, validation
- **Service**: Business logic, transactions
- **Repository**: Data access only
- **Domain**: Business rules, entities

---

## Controller Pattern

```java
@RestController
@RequestMapping("/v1/users")
public class UserController {
    
    private final UserService userService;
    private final UserQueryService userQueryService;
    
    public UserController(UserService userService, UserQueryService userQueryService) {
        this.userService = userService;
        this.userQueryService = userQueryService;
    }
    
    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        UserResponse response = userQueryService.findById(id);
        return ResponseEntity.ok(response);
    }
}
```

---

## Service Pattern (Command) - Interface + Implementation

```java
// Service Interface
public interface UserService {
    UserResponse create(CreateUserRequest request);
    void update(Long id, UpdateUserRequest request);
    void delete(Long id);
}

// Service Implementation
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    @Override
    @Transactional
    public UserResponse create(CreateUserRequest request) {
        User user = User.builder()
            .nickname(request.nickname())
            .email(request.email())
            .build();
        
        User saved = userRepository.save(user);
        eventPublisher.publishEvent(new UserCreatedEvent(saved.getId()));
        
        return UserResponse.from(saved);
    }
}
```

---

## Service Pattern (Query) - Interface + Implementation

```java
// Query Service Interface
public interface UserQueryService {
    UserResponse findById(Long id);
    List<UserResponse> findAll();
}

// Query Service Implementation
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryServiceImpl implements UserQueryService {
    
    private final UserRepository userRepository;
    
    @Override
    public UserResponse findById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
        return UserResponse.from(user);
    }
}
```

---

## DTO Pattern

### Naming Convention

| Type | Pattern | Example |
|------|---------|---------|
| Request | `<Action><Entity>Request` | `CreateUserRequest` |
| Response | `<Entity>Response` | `UserResponse` |
| Nested/Shared DTO | `<Parent><Child>Dto` | `CompanionDestinationDto` |
| Inter-module DTO | `<Entity><Purpose>Dto` | `UserProfileDto` |

### Request DTO
```java
public record CreateUserRequest(
    @NotBlank String email,
    @NotBlank String name,
    @Size(min = 8) String password
) {}
```

### Response DTO
```java
public record UserResponse(
    Long id,
    String email,
    String name,
    LocalDateTime createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getName(),
            user.getCreatedAt()
        );
    }
}
```

---

## Error Handling

### ErrorCode Interface
```java
public interface ErrorCode {
    HttpStatus getHttpStatus();
    String getCode();
    String getMessage();
}
```

### Domain ErrorCode
```java
@Getter
@AllArgsConstructor
public enum UserErrorCode implements ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    ;

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public String getCode() {
        return this.name();
    }
}
```

### Throwing Exceptions
```java
public User findById(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
}
```

---

## General Rules

- Prefer composition over inheritance
- Use Optional for nullable returns (except collections)
- Don't return null, throw exceptions or return empty collections
- Use meaningful variable names (no single letters except loop indices)
- Keep methods under 20 lines
- One class = one responsibility
