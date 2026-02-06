---
description: Create implementation plan with risk assessment
agent: planner
subtask: true
---

# Plan Command

Create a detailed implementation plan for: $ARGUMENTS

## Your Task

1. **Restate Requirements** - Clarify what needs to be built
2. **Analyze Codebase** - Review existing patterns in this Spring Boot project
3. **Identify Risks** - Surface potential issues, blockers, and dependencies
4. **Create Step Plan** - Break down implementation into phases
5. **Wait for Confirmation** - MUST receive user approval before proceeding

## Output Format

### Requirements Restatement
[Clear, concise restatement of what will be built]

### Codebase Analysis
- Affected modules: [user, feed, companion, etc.]
- Related existing code: [file paths]
- Patterns to follow: [existing patterns in codebase]

### Implementation Phases

**Phase 1: [Description]**
- Step 1.1: [specific action with file path]
- Step 1.2: ...

**Phase 2: [Description]**
- Step 2.1: ...

### Dependencies
- External: [APIs, services needed]
- Internal: [other modules this depends on]

### Risks
- **HIGH**: [Critical risks that could block implementation]
- **MEDIUM**: [Moderate risks to address]
- **LOW**: [Minor concerns]

### Skills to Load
- [ ] `coding-convention` - Required for all code
- [ ] `spring-modulith` - If crossing module boundaries
- [ ] `tdd` - If TDD approach requested

### Estimated Complexity
[HIGH/MEDIUM/LOW with time estimates]

---

**WAITING FOR CONFIRMATION**: Proceed with this plan? (yes/no/modify)

**CRITICAL**: Do NOT write any code until the user explicitly confirms.
