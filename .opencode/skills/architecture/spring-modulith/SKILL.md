---
name: spring-modulith
description: Spring Modulith patterns for modular monolith. Load when crossing module boundaries.
---

# Spring Modulith Patterns

## Module Structure

```
com.wit.<module>/
├── api/              # Controllers
├── application/      # Services (Command/Query)
├── domain/           # Entities
├── repository/       # JPA + QueryDSL
├── event/            # Domain events
├── dto/              # request/, response/
└── exception/        # ErrorCodes
```

## Module Boundary Rules

### FORBIDDEN
- Inject other module's Repository
- Reference other module's Entity
- Cross-module JPA associations

### ALLOWED

**Query (Read)**:
```java
// Call other module's QueryService
UserProfileDto user = userQueryService.getProfile(userId);
```

**Command (Write)**:
```java
// Publish event, don't call other module
eventPublisher.publishEvent(new CompanionCreatedEvent(id));
```

## CQRS Pattern

```java
// Command Service
public interface UserService {
    UserResponse create(CreateUserRequest req);
    void update(Long id, UpdateUserRequest req);
}

// Query Service
@Transactional(readOnly = true)
public interface UserQueryService {
    UserResponse findById(Long id);
    List<UserResponse> findAll();
}
```

## Event Pattern

```java
// Publishing
@Transactional
public void create(...) {
    var entity = repository.save(new Entity(...));
    eventPublisher.publishEvent(new EntityCreatedEvent(entity.getId()));
}

// Listening
@EventListener
@Async
public void handle(EntityCreatedEvent event) {
    // React to event
}
```

## Inter-Module Communication

| Purpose | Method | Return |
|---------|--------|--------|
| Read | QueryService | DTO |
| Write | Event | void |
