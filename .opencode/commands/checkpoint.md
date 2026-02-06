---
description: Save verification state and progress checkpoint
---

# Checkpoint Command

Save current verification state and create progress checkpoint: $ARGUMENTS

## Your Task

Create a snapshot of current progress including:

1. **Tests status** - Which tests pass/fail
2. **Build status** - Build succeeds or errors
3. **Code changes** - Summary of modifications
4. **Next steps** - What remains to be done

## Checkpoint Actions

### 1. Run Verification

```bash
# Build check
./gradlew build

# Test status
./gradlew test

# Lint check
./gradlew spotlessCheck
```

### 2. Capture State

```bash
# Changes since last commit
git diff --stat

# Untracked files
git status --short
```

## Checkpoint Format

```markdown
## Checkpoint: [Timestamp]

### Build Status
- [x] `./gradlew build` - PASSED
- [x] `./gradlew test` - PASSED (42/42)
- [x] `./gradlew spotlessCheck` - PASSED

### Changes Since Last Checkpoint
```
 src/main/java/com/wit/user/UserService.java | 24 ++++++
 src/test/java/com/wit/user/UserServiceTest.java | 48 ++++++++++++
 2 files changed, 72 insertions(+)
```

### Completed Tasks
- [x] UserService.createUser() 구현
- [x] Unit tests 작성
- [ ] Integration tests (in progress)

### Blocking Issues
- None

### Next Steps
1. Integration test 작성
2. Controller 구현
3. API 문서 작성

### Skills Loaded
- coding-convention
- tdd
```

## Usage in Development Flow

```
/plan -> implement -> /checkpoint -> continue -> /checkpoint -> /code-review
```

Use checkpoints to:
- Save state before risky changes
- Track progress through phases
- Enable easy rollback if needed
- Document verification points

---

**TIP**: Create checkpoints at natural breakpoints - after each phase, before major refactoring, after fixing bugs.
