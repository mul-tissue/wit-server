---
name: spring-modulith
description: Spring Modulith patterns for modular monolith architecture with CQRS and event-driven communication. Use when building features in this codebase.
---

# Spring Modulith Patterns

## When to Use This Skill
Use when building features in the modular monolith architecture using Spring Boot, Spring Modulith, CQRS, and event-driven communication between modules.

## Module Structure

Each module follows this structure:
```
com.wit.<module>/
├── api/              # REST Controllers
├── service/          # Business logic (Command/Query split)
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

## CQRS Implementation

Split Service layer into Command and Query:
- `*Service`: CUD operations, publishes events
- `*QueryService`: Read operations, returns DTOs

## Inter-Module Communication

### For Queries (Read)
- Call other module's `QueryService`
- Must return DTO (never Entity)

### For Commands (Write)
- Only modify your own domain
- Publish Domain Event after changes
- Other modules listen and react

See [references/inter-module-communication.md](references/inter-module-communication.md) for patterns.

## DTO Naming Convention

| Type | Pattern | Example |
|------|---------|---------|
| Request | `<Action><Entity>Request` | `CreateUserRequest` |
| Response | `<Entity>Response` | `UserResponse` |
| Nested/Shared | `<Parent><Child>Dto` | `CompanionDestinationDto` |
| Inter-module | `<Entity><Purpose>Dto` | `UserProfileDto` |

## Event-Driven Patterns

Events represent domain facts (past tense):
- `UserCreatedEvent`
- `CompanionJoinedEvent`
- `FeedPublishedEvent`

## Quick Start: Creating a New Feature

1. **Define Entity** in `domain/`
2. **Create Repositories** (JPA + QueryDSL)
3. **Create Services** (Command + Query)
4. **Create DTOs** in `dto/request/`, `dto/response/`
5. **Create Controller**
6. **Define Events** if needed
7. **Write Tests** (TDD)
