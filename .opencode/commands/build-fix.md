---
description: Diagnose and fix build errors
agent: build-error-resolver
subtask: true
---

# Build Fix Command

Fix build errors automatically.

## Usage

```
/build-fix
```

## What It Does

1. `./gradlew build` - capture error
2. Analyze error type
3. Apply fix
4. Verify build passes

## Supported Errors

- Compilation (missing imports, types)
- Dependency (missing, conflicts)
- Annotation processing (Lombok, QueryDSL)
- Spring config (beans, circular deps)
- Test failures

## Output

```
## Error
[exact message]

## Cause
[1 sentence]

## Fix
File: path/to/file.java
[change made]

## Verification
- Build: PASS
- Tests: PASS
```
