---
name: spring-modulith
description: Spring Modulith patterns for modular monolith architecture with CQRS and event-driven communication. Use when building features that span modules or creating new modules.
---

# Spring Modulith Patterns

## When to Use This Skill
Use when building features in the modular monolith architecture using Spring Boot, Spring Modulith, CQRS, and event-driven communication between modules.

## Architecture Style
- **Modular Monolith** using Spring Modulith
- Single JAR, package-level module separation
- Event-Driven Architecture for inter-module communication
- CQRS (conceptual separation at Service layer only)

## Module Structure

Each module follows this structure:
```
com.wit.<module>/
├── api/              # REST Controllers
├── application/      # Services (Command/Query split)
├── domain/           # Entities
├── repository/       # Data access (JPA + QueryDSL)
├── event/            # Domain events
├── dto/
│   ├── request/      # Request DTOs
│   ├── response/     # Response DTOs
│   └── *Dto.java     # Shared/Nested DTOs
└── exception/        # Module-specific exceptions
```

See [references/module-structure.md](references/module-structure.md) for detailed examples.

---

## Module Boundary Rules

### FORBIDDEN
- Injecting other module's Repository
- Direct reference to other module's Entity
- Cross-domain access via JPA associations
- Lazy loading other domain's data

### ALLOWED

**For Read (Query):**
- Call other module's `QueryService`
- Must return DTO only (never Entity)
- Example:
  ```java
  @Service
  public class FeedQueryService {
      private final UserQueryService userQueryService; // OK
      
      public FeedDetailDTO getDetail(Long feedId) {
          UserProfileDTO user = userQueryService.getProfile(userId); // OK
          // ...
      }
  }
  ```

**For Create/Update/Delete (Command):**
- Only modify own domain
- Publish Domain Event after changes
- Other modules react via Event Listener
- Example:
  ```java
  @Service
  @Transactional
  public class CompanionService {
      private final ApplicationEventPublisher eventPublisher;
      
      public void create(CompanionRequest request) {
          Companion companion = repository.save(new Companion(...));
          eventPublisher.publishEvent(new CompanionCreatedEvent(companion.getId()));
      }
  }
  ```

---

## CQRS Implementation

Split Service layer into Command and Query:
- `*Service`: CUD operations, publishes events
- `*QueryService`: Read operations, returns DTOs

### Service Layer Pattern
```java
// Command Service (Interface)
public interface UserService {
    User create(...);
    void update(...);
    void delete(...);
}

// Command Service (Implementation)
@Service
public class UserServiceImpl implements UserService {
    @Transactional
    public User create(...) { }
    
    @Transactional
    public void update(...) { }
    
    @Transactional
    public void delete(...) { }
}

// Query Service (Interface)
public interface UserQueryService {
    UserDTO findById(Long id);
    List<UserDTO> findByCondition(...);
}

// Query Service (Implementation)
@Service
@Transactional(readOnly = true)
public class UserQueryServiceImpl implements UserQueryService {
    public UserDTO findById(Long id) { }
    public List<UserDTO> findByCondition(...) { }
}
```

### Controller Layer (NOT split)
```java
@RestController
@RequestMapping("/v1/users")
public class UserController {
    private final UserService userService;
    private final UserQueryService userQueryService;
    
    // Both CUD and Query in same controller
    @PostMapping
    public ResponseEntity<UserResponse> create(@RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.create(request));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userQueryService.findById(id));
    }
}
```

### Repository Layer (Split)
```java
public interface UserRepository extends JpaRepository<User, Long> {
    // CUD operations
}

@Repository
public class UserQueryRepository {
    private final JPAQueryFactory queryFactory;
    // QueryDSL for complex queries
}
```

---

## Inter-Module Communication

| Purpose | Method | Return | Sync/Async |
|---------|--------|--------|------------|
| Query current state | QueryService | DTO | Sync |
| Notify changes | Domain Event | void | Async |

See [references/inter-module-communication.md](references/inter-module-communication.md) for patterns.

**Do NOT use:**
- Port/Adapter pattern
- Hexagonal Architecture layers
- Client interfaces for other modules

**Instead:**
- Service itself acts as the module API
- QueryService for read operations
- Events for write notifications

---

## Event-Driven Patterns

Events represent domain facts (past tense):
- `UserCreatedEvent`
- `CompanionJoinedEvent`
- `FeedPublishedEvent`

### Event Publishing
```java
@Service
@Transactional
public class UserService {
    private final ApplicationEventPublisher eventPublisher;
    
    public UserResponse create(UserRequest request) {
        User user = userRepository.save(new User(...));
        eventPublisher.publishEvent(new UserCreatedEvent(user.getId()));
        return UserResponse.from(user);
    }
}
```

### Event Listening
```java
@Component
public class NotificationEventListener {
    @EventListener
    @Async // Optional: for non-blocking
    public void handleUserCreated(UserCreatedEvent event) {
        notificationService.sendWelcomeNotification(event.userId());
    }
}
```

### Event Guidelines
- Events are fire-and-forget (no return value)
- Currently: ApplicationEvent (in-process)
- Future: Kafka/RabbitMQ (when moving to MSA)

---

## DTO Naming Convention

| Type | Pattern | Example |
|------|---------|---------|
| Request | `<Action><Entity>Request` | `CreateUserRequest` |
| Response | `<Entity>Response` | `UserResponse` |
| Nested/Shared | `<Parent><Child>Dto` | `CompanionDestinationDto` |
| Inter-module | `<Entity><Purpose>Dto` | `UserProfileDto` |

---

## Database Strategy
- Single PostgreSQL database (for now)
- No JPA associations between modules
- FK constraints at DB level only (can be removed if needed)
- Design for future DB-per-module migration

---

## Quick Start: Creating a New Feature

1. **Define Entity** in `domain/`
2. **Create Repositories** (JPA + QueryDSL)
3. **Create Services** (Command + Query)
4. **Create DTOs** in `dto/request/`, `dto/response/`
5. **Create Controller**
6. **Define Events** if needed
7. **Write Tests** (TDD)

---

## Core Principles
1. Enforce module boundaries via code rules
2. Access other domain's data as read-only
3. Changes via events, queries via sync calls
4. Don't create abstractions you don't need yet
