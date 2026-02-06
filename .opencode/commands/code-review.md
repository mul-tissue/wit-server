---
description: Review code for quality, security, and maintainability
agent: code-reviewer
subtask: true
---

# Code Review Command

Review code changes for quality, security, and maintainability: $ARGUMENTS

## Your Task

1. **Get changed files**: Run `git diff --name-only HEAD~1` or specified scope
2. **Load skill if needed**: `coding-convention` for Java/Spring patterns
3. **Analyze each file** for issues
4. **Generate structured report**

## Check Categories

### Security Issues (CRITICAL - Block)
- [ ] Hardcoded credentials, API keys, tokens
- [ ] SQL injection (raw queries instead of JPA/QueryDSL)
- [ ] Missing input validation on DTOs (`@Valid`, `@NotNull`, etc.)
- [ ] Missing authentication on endpoints
- [ ] Exposed sensitive data in responses
- [ ] Insecure dependencies

### Code Quality (HIGH - Should Fix)
- [ ] Methods > 30 lines
- [ ] Classes > 500 lines
- [ ] Missing error handling
- [ ] N+1 query patterns
- [ ] Missing `@Transactional` where needed
- [ ] Violation of Spring Modulith boundaries

### Best Practices (MEDIUM - Recommend)
- [ ] Missing JSDoc/JavaDoc for public APIs
- [ ] Inconsistent naming conventions
- [ ] Missing tests for new code
- [ ] TODO/FIXME without issue reference

### Style (LOW - Optional)
- [ ] Formatting issues (run Spotless)
- [ ] Import ordering

## Report Format

```
## Code Review Report

### Summary
- Files reviewed: X
- Critical: X | High: X | Medium: X | Low: X

### Critical Issues (Must Fix)

**[CRITICAL]** `UserService.java:42`
- Issue: Hardcoded secret key
- Fix: Move to application.yml with @Value

### High Priority (Should Fix)
...

### Recommendations
...
```

## Decision

- **CRITICAL issues**: Block merge, require fixes
- **HIGH issues**: Recommend fixes before merge
- **MEDIUM/LOW**: Informational

---

**IMPORTANT**: Never approve code with security vulnerabilities!
