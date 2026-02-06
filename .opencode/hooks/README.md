# Hooks

Event-based automation triggers for OpenCode.

## What are Hooks?

Hooks automatically execute commands when specific events occur:
- `PreToolUse` - Before a tool is executed
- `PostToolUse` - After a tool is executed
- `SessionStart` - When a session starts
- `SessionEnd` - When a session ends

## Future Hooks to Consider

### Auto-Format Java Files
```json
{
  "matcher": "tool == \"Edit\" && tool_input.file_path matches \"\\.java$\"",
  "hooks": [{
    "type": "command",
    "command": "./gradlew spotlessApply"
  }]
}
```

### Auto-Run Tests After Changes
```json
{
  "matcher": "tool == \"Write\" && tool_input.file_path matches \"src/main/.*\\.java$\"",
  "hooks": [{
    "type": "command", 
    "command": "./gradlew test"
  }]
}
```

### Build Error Detection
```json
{
  "matcher": "tool == \"Bash\" && tool_output matches \"BUILD FAILED\"",
  "hooks": [{
    "type": "command",
    "command": "Trigger build-error-resolver agent"
  }]
}
```

## How to Add Hooks

1. Create `hooks.json` in this directory
2. Define matchers and hook actions
3. Reload OpenCode configuration

## Reference

See everything-claude-code repository for hook examples:
`/Users/ohhyungsuh/Documents/everything-claude-code/hooks/hooks.json`
