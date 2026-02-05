---
name: architecture
description: Modular Monolith architecture rules and module boundaries
---

# Architecture Rules

## Architecture Style
- **Modular Monolith** using Spring Modulith
- Single JAR, package-level module separation
- Event-Driven Architecture for inter-module communication
- CQRS (conceptual separation at Service layer only)

## Module Structure
```
com.wit
├── common/        # 공통 모듈 (예외, 응답, 설정)
├── infra/         # 인프라 설정 (JPA, Redis, Properties)
├── user/
├── authentication/
├── authorization/
├── feed/
├── companion/
├── chat/
├── home/
└── notification/
```

### Common Module (공통)
```
com.wit.common/
├── config/                    # 공통 Config
│   └── swagger/
│       └── SwaggerConfig.java
├── response/                  # API 응답
│   ├── ApiResponse.java
│   └── ApiResponseAdvice.java
├── exception/                 # 예외 처리
│   ├── code/
│   │   ├── ErrorCode.java
│   │   └── GlobalErrorCode.java
│   ├── dto/
│   │   └── ErrorResponse.java
│   ├── CustomException.java
│   └── handler/
│       └── GlobalExceptionHandler.java
├── entity/                    # 공통 Entity
│   └── BaseTimeEntity.java
└── util/                      # 유틸리티
```

### Infra Module (인프라)
```
com.wit.infra/
├── config/                    # 인프라 Config
│   ├── JpaAuditingConfig.java
│   └── RedisConfig.java
├── properties/                # @ConfigurationProperties
│   ├── jwt/
│   │   └── JwtProperties.java
│   └── redis/
│       └── RedisProperties.java
└── security/                  # Security 관련
    ├── config/
    │   ├── SecurityConfig.java
    │   └── CorsConfig.java
    ├── filter/
    │   └── JwtFilter.java
    └── jwt/
        └── JwtUtil.java
```

### Domain Module (도메인별)
```
module/
├── api/           # Controllers
├── application/   # Services (Command/Query split)
├── domain/        # Entities, Value Objects
├── repository/    # Data access
├── dto/
│   ├── request/
│   └── response/
├── exception/     # 도메인별 ErrorCode
│   └── XxxErrorCode.java
└── event/         # Domain events (필요시)
```

## Module Boundary Rules

### ❌ FORBIDDEN
- Injecting other module's Repository
- Direct reference to other module's Entity
- Cross-domain access via JPA associations
- Lazy loading other domain's data

### ✅ ALLOWED

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

## Inter-Module Communication

| Purpose | Method | Return | Sync/Async |
|---------|--------|--------|------------|
| Query current state | QueryService | DTO | Sync |
| Notify changes | Domain Event | void | Async |

**Do NOT use:**
- Port/Adapter pattern
- Hexagonal Architecture layers
- Client interfaces for other modules

**Instead:**
- Service itself acts as the module API
- QueryService for read operations
- Events for write notifications

## CQRS Application

**Service Layer (Split):**
```java
@Service
public class UserService {        // CUD operations
    public User create(...) { }
    public void update(...) { }
    public void delete(...) { }
}

@Service
public class UserQueryService {   // Read operations
    public UserDTO findById(...) { }
    public List<UserDTO> findAll(...) { }
}
```

**Controller Layer (NOT split):**
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final UserQueryService userQueryService;
    
    // Both CUD and Query in same controller
}
```

**Repository Layer (Split):**
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

## Database Strategy
- Single PostgreSQL database (for now)
- No JPA associations between modules
- FK constraints at DB level only (can be removed if needed)
- Design for future DB-per-module migration

## Event Guidelines
- Events represent domain facts (past tense): `UserCreatedEvent`, `CompanionJoinedEvent`
- Events are fire-and-forget (no return value)
- Currently: ApplicationEvent (in-process)
- Future: Kafka/RabbitMQ (when moving to MSA)

## Core Principles
1. Enforce module boundaries via code rules
2. Access other domain's data as read-only
3. Changes via events, queries via sync calls
4. Don't create abstractions you don't need yet
