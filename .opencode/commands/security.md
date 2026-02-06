---
description: Security vulnerability review
agent: security-reviewer
subtask: true
---

# Security Review Command

Review security: $ARGUMENTS

## Checklist

### CRITICAL
- [ ] No hardcoded secrets
- [ ] All inputs validated (@Valid)
- [ ] No SQL injection
- [ ] Auth on all endpoints
- [ ] JWT properly validated

### HIGH
- [ ] Sensitive data not exposed
- [ ] No PII in logs
- [ ] CORS configured
- [ ] Actuator secured

## Output

```
[CRITICAL] SQL Injection
File: UserRepo.java:25
// BAD
"SELECT * FROM users WHERE name = '" + name + "'"
// FIX
@Query("SELECT u FROM User u WHERE u.name = :name")
```

**RULE**: Security issues are blockers. No exceptions.
