---
description: Dead code removal and consolidation
agent: refactor-cleaner
subtask: true
---

# Refactor Clean Command

Clean up: $ARGUMENTS

## Steps

1. `./gradlew test` - ensure tests pass
2. Find unused code
3. Remove safely
4. `./gradlew test` - verify still passes

## Find Dead Code

- Unused imports
- Unused private methods
- Commented code
- Unused classes/DTOs

## Safe Removal Order

1. Unused imports
2. Unused private methods
3. Unused public methods (verify callers)
4. Unused classes

## Output

```
## Removed
- OldService.java - replaced by NewService
- utils.formatDate() - no callers

## Consolidated
- validateEmail() x3 -> EmailValidator

## Tests: PASS
```

**RULE**: Tests must pass before AND after. Always verify.
