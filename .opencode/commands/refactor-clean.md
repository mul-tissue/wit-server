---
description: Remove dead code and consolidate duplicates
agent: refactor-cleaner
subtask: true
---

# Refactor Clean Command

Analyze and clean up the codebase: $ARGUMENTS

## Your Task

1. **Detect dead code** - unused classes, methods, imports
2. **Identify duplicates** - consolidation opportunities
3. **Safely remove** - with verification
4. **Ensure tests pass** - no functionality broken

## Detection Phase

### Find Unused Code

```bash
# Find unused imports (IDE or tools)
./gradlew spotlessCheck

# Check for unused dependencies
./gradlew dependencyInsight --dependency <name>

# Find unused methods (manual review or IDE inspection)
```

### Manual Checks

- [ ] Unused private methods
- [ ] Unused public methods (check all callers)
- [ ] Commented-out code blocks
- [ ] Unused DTOs/Entities
- [ ] Dead feature flags
- [ ] Orphaned test files

## Removal Phase

### Before Removing

1. **Search for usage** - grep, IDE find usages
2. **Check Spring injection** - might be used via DI
3. **Check reflection** - `@ConditionalOn*`, dynamic calls
4. **Verify tests** - no test depends on it
5. **Document removal** - in commit message

### Safe Removal Order

1. Remove unused imports first
2. Remove unused private methods
3. Remove unused public methods (verify no callers)
4. Remove unused classes
5. Remove unused files

## Consolidation Phase

### Identify Duplicates

- Similar utility methods across modules
- Copy-pasted validation logic
- Repeated exception handling patterns
- Duplicate DTOs

### Consolidation Strategies

```java
// Before: Duplicate validation in multiple services
public void validateUser(User user) { ... }  // UserService
public void validateUser(User user) { ... }  // AdminService

// After: Extract to shared utility
@Component
public class UserValidator {
    public void validate(User user) { ... }
}
```

## Verification

After cleanup:

```bash
# 1. Build succeeds
./gradlew build

# 2. All tests pass
./gradlew test

# 3. No lint errors
./gradlew spotlessCheck

# 4. Application starts
./gradlew bootRun
```

## Report Format

```markdown
## Refactor Clean Report

### Removed
- `OldUserService.java` - Replaced by UserService
- `UserDto.legacyField` - Unused since v2.0
- `HelperUtils.formatDate()` - No callers

### Consolidated
- `validateEmail()` x3 -> `EmailValidator.validate()`
- Duplicate exception handlers -> `GlobalExceptionHandler`

### Manual Review Needed
- `LegacyController.java` - Verify with team before removing
```

---

**CAUTION**: Always verify before removing. When in doubt, add `// TODO: verify usage` comment.
