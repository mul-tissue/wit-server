---
name: spring-modulith
description: Spring Modulith patterns for modular monolith architecture with CQRS and event-driven communication
---

# Spring Modulith Patterns

## When to Use This Skill
Use when building features in the modular monolith architecture using Spring Boot, Spring Modulith, CQRS, and event-driven communication between modules.

## Module Structure

Each module follows this structure:
```
com.wit.<module>/
├── api/          # REST Controllers
├── service/      # Business logic (Command/Query split)
├── domain/       # Entities
├── repository/   # Data access (JPA + QueryDSL)
├── event/        # Domain events
└── dto/          # Request/Response/Transfer objects
```

See [references/module-structure.md](./references/module-structure.md) for detailed examples.

## CQRS Implementation

Split Service layer into Command and Query:
- `*Service`: CUD operations, publishes events
- `*QueryService`: Read operations, returns DTOs

See [references/cqrs-pattern.md](./references/cqrs-pattern.md) for implementation details.

## Inter-Module Communication

### For Queries (Read)
- Call other module's `QueryService`
- Must return DTO (never Entity)

### For Commands (Write)
- Only modify your own domain
- Publish Domain Event after changes
- Other modules listen and react

See [references/inter-module-communication.md](./references/inter-module-communication.md) for patterns.

## Event-Driven Patterns

Events represent domain facts (past tense):
- `UserCreatedEvent`
- `CompanionJoinedEvent`
- `FeedPublishedEvent`

See [references/event-patterns.md](./references/event-patterns.md) for event handling.

## Repository Patterns

- Use JPA Repository for simple CRUD
- Use QueryDSL for complex queries
- Split into `*Repository` and `*QueryRepository`

See [references/repository-patterns.md](./references/repository-patterns.md) for examples.

## Quick Start: Creating a New Feature

1. **Define Entity** in `domain/`
2. **Create Repositories** (JPA + QueryDSL)
3. **Create Services** (Command + Query)
4. **Create Controller** with DTOs
5. **Define Events** if needed
6. **Write Tests** (TDD)

For step-by-step guide, see [references/feature-creation-guide.md](./references/feature-creation-guide.md)
