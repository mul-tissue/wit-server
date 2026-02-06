# Agent Orchestration Rules

## Available Agents

| Agent | Purpose | When to Use |
|-------|---------|-------------|
| `planner` | Implementation planning | Complex features |
| `architect` | System design | Architectural decisions |
| `tdd-guide` | Test-driven development | New features, bug fixes |
| `code-reviewer` | Code quality review | After code changes |
| `security-reviewer` | Security analysis | Before commits |
| `database-reviewer` | DB/JPA optimization | N+1, queries |
| `build-error-resolver` | Fix build errors | Build failures |
| `refactor-cleaner` | Dead code cleanup | Maintenance |

## Proactive Agent Usage (No User Prompt Needed)

1. **Complex feature** → `planner` first
2. **Code written** → `code-reviewer`
3. **Bug fix/new feature** → `tdd-guide`
4. **Architecture decision** → `architect`
5. **Build failure** → `build-error-resolver`
6. **DB changes** → `database-reviewer`

## Agent Execution Order

### New Feature
```
planner → tdd-guide → implement → code-reviewer → database-reviewer
```

### Bug Fix
```
tdd-guide → implement → code-reviewer
```

### Build Error
```
build-error-resolver → code-reviewer
```

## Token Optimization Rules

All agents MUST:
1. **NO internal thought process** - Output results only
2. **Keep brief** - Max 50 lines output per issue
3. **Structured format** - Use headers and bullets
4. **No theory** - Procedures and checklists only

## User Feedback Rules

### Ask User Before:
- Deleting files/directories
- Dropping database tables
- Multiple valid approaches exist
- Unclear requirements
- Significant architectural decisions
- Pushing to remote

### Proceed Without Asking:
- Following established conventions (skills loaded)
- Creating/modifying files (reversible via git)
- Running tests, builds, formatting
- Implementing CRUD following patterns
- Writing tests
- Using agents proactively

## Agent Output Standards

1. Severity levels: CRITICAL, HIGH, MEDIUM, LOW
2. Include file paths and line numbers
3. Provide fix examples
4. Brief summary at end
