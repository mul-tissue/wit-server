---
description: Run comprehensive security review for Spring Boot
agent: security-reviewer
subtask: true
---

# Security Review Command

Conduct a comprehensive security review: $ARGUMENTS

## Your Task

Analyze the specified code for security vulnerabilities following OWASP guidelines and Spring Security best practices.

## Security Checklist

### 1. Authentication & Authorization (CRITICAL)

- [ ] All endpoints have proper `@PreAuthorize` or security config
- [ ] JWT validation is correct (signature, expiration, claims)
- [ ] Password hashing uses BCrypt or Argon2
- [ ] Session management is secure
- [ ] OAuth2/OIDC properly configured (if used)

### 2. Input Validation (CRITICAL)

- [ ] All DTOs use Bean Validation (`@Valid`, `@NotNull`, `@Size`, etc.)
- [ ] Path variables validated
- [ ] No SQL injection (JPA/QueryDSL only, no raw queries)
- [ ] No command injection
- [ ] File upload validation (type, size, name)

### 3. Data Exposure (HIGH)

- [ ] Sensitive fields excluded from responses (`@JsonIgnore`)
- [ ] No stack traces in error responses
- [ ] Proper logging (no PII in logs)
- [ ] API responses don't leak internal structure

### 4. Configuration Security (HIGH)

- [ ] No hardcoded secrets in code
- [ ] Secrets in environment variables or Vault
- [ ] application.yml doesn't contain production secrets
- [ ] CORS properly configured
- [ ] CSRF protection enabled (or properly disabled for APIs)

### 5. Dependency Security (MEDIUM)

```bash
# Check for vulnerable dependencies
./gradlew dependencyCheckAnalyze
```

- [ ] No known CVEs in dependencies
- [ ] Dependencies up to date

### 6. Spring-Specific Checks

- [ ] `@Transactional` doesn't expose data on rollback
- [ ] Entity IDs not directly exposed (use UUIDs or DTOs)
- [ ] Lazy loading doesn't cause data leaks
- [ ] Actuator endpoints secured

## Report Format

```markdown
# Security Review Report

**Scope:** [files/modules reviewed]
**Date:** YYYY-MM-DD
**Risk Level:** HIGH / MEDIUM / LOW

## Critical Issues (Fix Immediately)

### 1. [Issue Title]
- **Location:** `UserController.java:42`
- **Vulnerability:** SQL Injection
- **Impact:** Database compromise
- **Fix:**
```java
// Before (vulnerable)
@Query("SELECT u FROM User u WHERE u.name = '" + name + "'")

// After (safe)
@Query("SELECT u FROM User u WHERE u.name = :name")
User findByName(@Param("name") String name);
```

## High Priority
...

## Recommendations
...
```

---

**IMPORTANT**: Security issues are blockers. Do not proceed until critical issues are resolved.
