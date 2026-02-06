---
description: Code quality and security review
agent: code-reviewer
subtask: true
---

# Code Review Command

Review: $ARGUMENTS

## Steps

1. `git diff` to see changes
2. Check categories below
3. Output findings

## Categories

### CRITICAL (Block)
- Hardcoded secrets
- SQL injection
- Missing auth
- N+1 queries

### HIGH (Should Fix)
- Methods > 30 lines
- Missing @Transactional
- Modulith violations

### MEDIUM (Recommend)
- Missing tests
- Poor naming

## Output

```
[CRITICAL] <issue>
File: path:line
Fix: <solution>

[HIGH] <issue>
...
```

## Verdict

- CRITICAL/HIGH → Request changes
- MEDIUM/LOW only → Approve
