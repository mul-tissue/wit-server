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
    
    public void setNickname(String nickname) { // NEVER
        this.nickname = nickname;
    }
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
    public void updateProfile(String nickname, String profileImagePath) {
        this.nickname = nickname;
        this.profileImagePath = profileImagePath;
    }
    
    // State transition with validation
    public void activate() {
        if (this.status != UserStatus.PENDING_ONBOARDING) {
            throw new IllegalStateException("Cannot activate user in status: " + this.status);
        }
        this.status = UserStatus.ACTIVE;
    }
    
    // Explicit withdrawal with timestamp
    public void withdraw() {
        this.status = UserStatus.WITHDRAWN;
        this.withdrawnAt = LocalDateTime.now();
    }
}
```

### Why No Setters?
1. **Intent is clear**: `user.withdraw()` is clearer than `user.setStatus(WITHDRAWN)`
2. **Validation**: Business methods can validate state transitions
3. **Encapsulation**: Internal state changes are controlled
4. **Auditability**: Side effects (timestamps, events) are handled consistently

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
│   └── <Entity><Purpose>Dto.java     # nested/shared
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

// Field injection (avoid)
@Service
public class UserService {
    @Autowired // AVOID
    private UserRepository userRepository;
}
```

### Layer Responsibilities
- **Controller**: HTTP request/response, validation
- **Service**: Business logic, transactions
- **Repository**: Data access only
- **Domain**: Business rules, entities

### File Naming
- Controllers: `*Controller.java`
- Services: `*Service.java` (interface), `*ServiceImpl.java` (implementation)
- Query Services: `*QueryService.java` (interface), `*QueryServiceImpl.java` (implementation)
- Repositories: `*Repository.java`, `*QueryRepository.java`
- DTOs: `dto/request/*Request.java`, `dto/response/*Response.java`, `dto/*Dto.java`
- Events: `*Event.java` (past tense, e.g., `UserCreatedEvent`)

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
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        UserResponse response = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getById(@PathVariable Long id) {
        UserDTO user = userQueryService.findById(id);
        return ResponseEntity.ok(user);
    }
}
```

---

## Service Pattern (Command) - Interface + Implementation

```java
// Service Interface
public interface UserService {
    UserResponse create(UserRequest request);
    void update(Long id, UserRequest request);
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
    public UserResponse create(UserRequest request) {
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
    UserDTO findById(Long id);
    List<UserDTO> findAll();
}

// Query Service Implementation
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryServiceImpl implements UserQueryService {
    
    private final UserRepository userRepository;
    
    @Override
    public UserDTO findById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        return UserDTO.from(user);
    }
}
```

---

## DTO Pattern

### Package Structure
```
dto/
├── request/
│   ├── CreateUserRequest.java
│   └── UpdateUserRequest.java
├── response/
│   ├── UserResponse.java
│   └── UserDetailResponse.java
└── UserProfileDto.java          # nested/shared DTO
```

### Naming Convention

| Type | Pattern | Example |
|------|---------|---------|
| Request | `<Action><Entity>Request` | `CreateUserRequest`, `UpdateCompanionRequest` |
| Response | `<Entity>Response`, `<Entity><Detail>Response` | `UserResponse`, `CompanionDetailResponse` |
| Nested/Shared DTO | `<Parent><Child>Dto` | `CompanionDestinationDto`, `FeedImageDto` |
| Inter-module DTO | `<Entity><Purpose>Dto` | `UserProfileDto`, `CompanionSummaryDto` |

### Request DTO
```java
// dto/request/CreateUserRequest.java
public record CreateUserRequest(
    @NotBlank String email,
    @NotBlank String name,
    @Size(min = 8) String password
) {}
```

### Response DTO
```java
// dto/response/UserResponse.java
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

### Nested DTO (separate file)
```java
// dto/CompanionDestinationDto.java
public record CompanionDestinationDto(
    String city,
    String country,
    LocalDate arrivalDate,
    LocalDate departureDate
) {
    public static CompanionDestinationDto from(CompanionDestination destination) {
        return new CompanionDestinationDto(
            destination.getCity(),
            destination.getCountry(),
            destination.getArrivalDate(),
            destination.getDepartureDate()
        );
    }
}
```

---

## Event Pattern

```java
// Event class (past tense)
public record UserCreatedEvent(Long userId) {}

// Event listener (in another module)
@Component
public class NotificationEventListener {
    
    private final NotificationService notificationService;
    
    public NotificationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    
    @EventListener
    public void handleUserCreated(UserCreatedEvent event) {
        notificationService.sendWelcomeNotification(event.userId());
    }
}
```

---

## Response & Error Handling

### Error Response Format
```json
{
  "errorName": "USER_NOT_FOUND",
  "message": "사용자를 찾을 수 없습니다."
}
```

### ErrorCode Interface
```java
public interface ErrorCode {
    HttpStatus getHttpStatus();
    String getMessage();
    String getErrorName();  // enum name() return
}
```

### Domain ErrorCode Example
```java
@Getter
@AllArgsConstructor
public enum UserErrorCode implements ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    INVALID_USER_STATUS(HttpStatus.BAD_REQUEST, "유효하지 않은 사용자 상태입니다."),
    ;

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public String getErrorName() {
        return this.name();
    }
}
```

### Throwing Exceptions
```java
// In Service
public User findById(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
}
```

### Controller Response
```java
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
        UserResponse response = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        UserResponse response = userQueryService.findById(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
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
