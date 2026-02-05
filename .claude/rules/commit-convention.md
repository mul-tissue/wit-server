---
name: commit-convention
description: Git commit message format and rules
---

# Commit Convention

All commits must follow this format:

```
<type>(<scope>): <subject>

<body>
```

## Types
- `feat`: New feature
- `fix`: Bug fix
- `refactor`: Code refactoring (no behavior change)
- `test`: Add or update tests
- `docs`: Documentation changes
- `chore`: Build scripts, dependencies, configs
- `perf`: Performance improvements

## Scope
Use module name as scope:
- `user`, `feed`, `companion`, `chat`, `notification`, `auth`
- `common` for shared utilities
- `config` for configurations

## Examples
```
feat(user): add user registration API
fix(auth): resolve JWT token expiration issue
refactor(feed): extract query logic to QueryService
test(companion): add integration tests for join flow
docs(readme): update architecture diagram
```

## Rules
- Subject: lowercase, no period at end, max 50 chars
- Use imperative mood ("add" not "added")
- Body: optional, wrap at 72 chars
- Reference issues if applicable: `Closes #123`

## Bad Examples
```
❌ Added user feature
❌ Fix bug
❌ update code
❌ feat: very long subject line that exceeds fifty characters limit
```

## Good Examples
```
✅ feat(user): add user profile update endpoint
✅ fix(chat): prevent duplicate message sending
✅ refactor(feed): split command and query services
```
