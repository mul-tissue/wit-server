---
description: Create implementation plan and GitHub issue
agent: planner
subtask: true
---

# Plan Command

Feature to plan: $ARGUMENTS

## Workflow

1. **Analyze** - Understand requirements and codebase
2. **Plan** - Create implementation phases
3. **Confirm** - Wait for user approval
4. **Create Issue** - `gh issue create` with plan
5. **Create Branch** - `git checkout -b feat/#<issue>-<name>`

## Output Format (Keep Brief)

```markdown
## Summary
[1-2 sentences]

## Phases
1. [Phase]: [steps]
2. [Phase]: [steps]

## Risks
- [risk if any]

## Complexity: [HIGH/MEDIUM/LOW]
```

**Ask**: "Proceed? (yes/no)"

## After Confirmation

```bash
# Create GitHub issue
gh issue create --title "[Feature] <name>" --body "<plan>"

# Create branch
git checkout -b feat/#<issue-number>-<feature-name>
```

Notify: "Issue #X created, branch `feat/#X-name` ready"

**CRITICAL**: No code until user confirms. Create issue BEFORE coding.
