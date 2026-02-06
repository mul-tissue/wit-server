# Git Workflow Rules

## Issue-Driven Development

**Every code change must be linked to an issue.**

### Workflow
1. Create GitHub issue first (`gh issue create`)
2. Create branch from issue: `feat/#123-feature-name`
3. Develop with small, frequent commits
4. Create PR linking to issue: `Closes #123`
5. Code review and merge

## Branch Naming Convention

```
<type>/#<issue-number>-<short-description>
```

### Types
| Type | Usage |
|------|-------|
| `feat` | New feature |
| `fix` | Bug fix |
| `refactor` | Code refactoring |
| `test` | Test additions/changes |
| `docs` | Documentation |
| `chore` | Maintenance tasks |

### Examples
```
feat/#123-user-signup
fix/#456-login-timeout
refactor/#789-extract-validation
chore/#101-update-dependencies
```

## Commit Convention

```
<type>(<scope>): <subject>

[optional body]

[optional footer]
```

### Types
`feat`, `fix`, `refactor`, `test`, `docs`, `chore`, `perf`

### Scopes (Module Names)
`user`, `feed`, `companion`, `chat`, `notification`, `auth`, `common`, `config`

### Examples
```
feat(user): add user registration API

fix(auth): resolve JWT token expiration issue

refactor(feed): extract query logic to QueryService

test(companion): add integration tests for join flow

chore(config): setup GitHub Actions CI workflow
```

## Commit Habits

**Commit small and often:**

| After | Commit |
|-------|--------|
| Entity creation | ✅ |
| Repository creation | ✅ |
| Service method implementation | ✅ |
| Test creation | ✅ |
| Controller implementation | ✅ |
| Bug fix | ✅ |

### Good Commit Size
- Single logical change
- Easy to review in < 5 minutes
- Easy to revert if needed

### Bad Commit Size
- Multiple unrelated changes
- "WIP" commits with broken code
- Huge commits with 1000+ lines

## Pull Request Rules

### Before Creating PR
- [ ] All tests pass (`./gradlew test`)
- [ ] Code formatting applied (`./gradlew spotlessApply`)
- [ ] No hardcoded secrets
- [ ] Meaningful commit messages

### PR Title Format
Same as commit convention:
```
feat(user): add user registration API
```

### PR Description Template
```markdown
## Summary
- Brief description of changes
- Why this change is needed

## Changes
- List of specific changes made

## Test Plan
- How the changes were tested
- Any manual testing needed

Closes #<issue-number>
```

### PR Size Guidelines
| Lines Changed | Size | Recommendation |
|---------------|------|----------------|
| < 100 | Small | Ideal |
| 100-300 | Medium | Acceptable |
| 300-500 | Large | Consider splitting |
| > 500 | Too Large | Must split |

## Protected Branches

### `main` / `master`
- No direct push
- Requires PR approval
- CI must pass

### `develop`
- No direct push (recommended)
- CI must pass on PR

## Git Commands Reference

### Create Branch from Issue
```bash
gh issue create --title "Feature X" --body "..."
# Returns issue #123

git checkout -b feat/#123-feature-x
```

### Create PR
```bash
gh pr create --title "feat(module): description" --body "Closes #123"
```

### Amend Last Commit (before push)
```bash
git commit --amend -m "new message"
```

### Rebase onto develop
```bash
git fetch origin
git rebase origin/develop
```

## Forbidden Actions

- ❌ Force push to `main`/`develop`
- ❌ Commit secrets/credentials
- ❌ Commit directly to protected branches
- ❌ Merge without CI passing
- ❌ Skip code review for significant changes
