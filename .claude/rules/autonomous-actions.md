---
name: autonomous-actions
description: Guidelines for when to proceed without asking vs when to ask for confirmation
---

# Autonomous Actions

This document defines when Claude Code can proceed autonomously vs when it should ask for human confirmation.

## ✅ Proceed Without Asking

You can proceed autonomously when:

### 1. Following Established Conventions
- Applying commit message format from `commit-convention.md`
- Using file naming conventions (e.g., `UserController`, `UserService`)
- Following package structure defined in `architecture.md`
- Implementing patterns from coding-style rules

### 2. Reversible Operations
- Creating new files
- Writing code (can be reverted via git)
- Adding tests
- Code refactoring
- Adding dependencies to build.gradle
- Updating documentation

### 3. Safe Operations
- Running tests (`./gradlew test`)
- Running builds (`./gradlew build`)
- Code formatting (Spotless)
- Generating code coverage reports
- Reading files/analyzing code

### 4. Standard Development Tasks
- Implementing CRUD operations following established patterns
- Writing unit tests for new code
- Creating DTOs following naming conventions
- Adding validation annotations
- Implementing exception handling

## ❓ Ask Before Proceeding

You MUST ask for confirmation when:

### 1. Destructive Operations
- Deleting files or directories
- Dropping database tables
- Removing dependencies
- `git reset --hard`
- `git push --force`
- Deleting Git branches

### 2. Multiple Valid Approaches
When there are several reasonable implementations and no clear convention:
- "Should I use JWT or Session-based authentication?"
- "Should I use H2 or PostgreSQL for this test?"
- "Should this return 200 OK or 201 Created?"
- "Should I create a new module or add to existing?"

### 3. Unclear Requirements
When the specification is ambiguous:
- Password validation rules (length, complexity)
- Pagination defaults (page size, max size)
- Error message formats
- API versioning strategy
- Caching TTL values

### 4. Significant Architectural Decisions
- Adding a new module to the project
- Introducing new frameworks or libraries
- Changing database schema significantly
- Modifying security configuration
- Changing module boundaries

### 5. External System Interactions
- Deploying to production
- Pushing to remote repository
- Calling external APIs in production mode
- Sending emails/notifications to real users
- Making database migrations in production

### 6. Cost/Performance Implications
- Operations that may incur significant costs
- Database indexes that may affect performance
- Caching strategies that use significant memory
- API rate limits

## Examples

### ✅ Autonomous Examples

```
User: "Add a user registration endpoint"
→ Proceed: This follows standard CRUD patterns and conventions

User: "Write tests for UserService"
→ Proceed: Tests are reversible and follow TDD rules

User: "Fix this build error"
→ Proceed: Fixing builds is safe and expected

User: "Refactor this code to follow CQRS"
→ Proceed: Refactoring is reversible via git

User: "Add validation to UserRequest"
→ Proceed: Follows security rules and is standard practice
```

### ❓ Ask First Examples

```
User: "Should we use WebSocket or SSE for chat?"
→ Ask: Multiple valid approaches, significant architectural impact

User: "Delete the old user table"
→ Ask: Destructive operation

User: "What should the password minimum length be?"
→ Ask: Specification unclear

User: "Push this to main branch"
→ Ask: External operation, affects team

User: "Should I create a new 'payment' module?"
→ Ask: Significant architectural decision
```

## When in Doubt

If you're unsure whether to proceed or ask:

1. **Check if it's reversible**: Can this be undone with `git revert`?
2. **Check if it follows conventions**: Is there a clear rule in `.claude/rules/`?
3. **Check impact**: Does this affect other developers or systems?

If any of these raise concerns, **ask first**.

## Exception Handling Philosophy

Think of asking for confirmation like throwing an exception in code:

```java
// Normal flow - autonomous
public void createUser(UserRequest request) {
    // Follow conventions, proceed autonomously
}

// Exception case - ask
public void createUser(UserRequest request) {
    if (unclear || destructive || multipleOptions) {
        throw new NeedHumanDecisionException();
    }
}
```

**Ask questions = Handle exceptions**
**Autonomous work = Normal execution flow**

## Communication Style

When you need to ask:
- **Be specific**: "Should I use BCrypt or Argon2 for password hashing?"
- **Provide context**: "The security rule says passwords must be encoded, but doesn't specify algorithm"
- **Suggest options**: "I recommend BCrypt because it's Spring Security's default"

When proceeding autonomously:
- **State what you're doing**: "Creating UserController following REST conventions"
- **Reference rules**: "Following architecture.md module structure"
- **Be transparent**: "Implementing this as CQRS with separate Service and QueryService"
