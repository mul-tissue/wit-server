---
description: TDD workflow - tests before code
agent: tdd-guide
subtask: true
---

# TDD Command

Implement using TDD: $ARGUMENTS

## Workflow

```
RED -> GREEN -> REFACTOR -> REPEAT
```

1. **RED**: Write failing test
2. **GREEN**: Minimal code to pass
3. **REFACTOR**: Improve, keep green

## Steps

1. Load skills: `coding-convention`, `tdd`
2. Define interface (DTO, method signature)
3. Write failing test
4. Implement to pass
5. Refactor
6. `./gradlew test`

## Test Template

```java
@Test
void shouldExpected_whenCondition() {
    // given
    
    // when
    
    // then
}
```

## Coverage

| Layer | Min |
|-------|-----|
| Service | 80% |
| Domain | 90% |

**RULE**: Test BEFORE implementation. No exceptions.
