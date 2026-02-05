---
name: coding-style
description: Java and Spring Boot coding standards following Google Java Style Guide
---

# Coding Style

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

## ❌ NO SETTERS in Domain Entities

**Setter methods are prohibited in domain entities.** Use business methods instead.

### ❌ BAD - Using Setters
```java
@Entity
public class User {
    @Setter // ❌ NEVER use @Setter
    private String nickname;
    
    public void setNickname(String nickname) { // ❌ NEVER
        this.nickname = nickname;
    }
    
    public void setStatus(UserStatus status) { // ❌ NEVER
        this.status = status;
    }
}

// Usage
user.setNickname("newName");
user.setStatus(UserStatus.ACTIVE);
```

### ✅ GOOD - Using Business Methods
```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    private String nickname;
    private UserStatus status;
    
    // ✅ Business method with clear intent
    public void updateProfile(String nickname, String profileImagePath) {
        this.nickname = nickname;
        this.profileImagePath = profileImagePath;
    }
    
    // ✅ State transition with validation
    public void activate() {
        if (this.status != UserStatus.PENDING_ONBOARDING) {
            throw new IllegalStateException("Cannot activate user in status: " + this.status);
        }
        this.status = UserStatus.ACTIVE;
    }
    
    // ✅ Explicit withdrawal with timestamp
    public void withdraw() {
        this.status = UserStatus.WITHDRAWN;
        this.withdrawnAt = LocalDateTime.now();
    }
    
    // ✅ Record login time
    public void recordLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }
}

// Usage - clear business intent
user.updateProfile("newName", "/images/profile.jpg");
user.activate();
user.withdraw();
user.recordLogin();
```

### Why No Setters?
1. **Intent is clear**: `user.withdraw()` is clearer than `user.setStatus(WITHDRAWN)`
2. **Validation**: Business methods can validate state transitions
3. **Encapsulation**: Internal state changes are controlled
4. **Auditability**: Side effects (timestamps, events) are handled consistently

## Spring Boot Patterns

### Dependency Injection
```java
// ✅ Constructor injection (preferred)
@Service
public class UserService {
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}

// ❌ Field injection (avoid)
@Service
public class UserService {
    @Autowired
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
│   └── <Entity><Purpose>Dto.java     # 중첩/공용
```

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
            .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
        return UserDTO.from(user);
    }
}
```

## Event Pattern
```java
// Event class
public record UserCreatedEvent(Long userId) {
}

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

## Exception Handling
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(e.getMessage()));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(message));
    }
}
```

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
└── UserProfileDto.java          # 중첩/공용 DTO
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

### Nested DTO (별도 파일)
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

// dto/request/CreateCompanionRequest.java
public record CreateCompanionRequest(
    @NotBlank String title,
    @NotBlank String description,
    @NotEmpty List<CompanionDestinationDto> destinations  // 별도 파일 참조
) {}

// dto/response/CompanionResponse.java
public record CompanionResponse(
    Long id,
    String title,
    List<CompanionDestinationDto> destinations  // 재사용
) {}
```

### Inter-module DTO
```java
// dto/UserProfileDto.java (다른 모듈에서 사용)
public record UserProfileDto(
    Long id,
    String name,
    String profileImageUrl
) {}
```

## General Rules
- Prefer composition over inheritance
- Use Optional for nullable returns (except collections)
- Don't return null, throw exceptions or return empty collections
- Use meaningful variable names (no single letters except loop indices)
- Keep methods under 20 lines
- One class = one responsibility
