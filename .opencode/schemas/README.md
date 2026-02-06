# Schemas

JSON Schema definitions for configuration validation.

## Purpose

Schemas provide:
- Configuration file validation
- IDE autocomplete for config files
- Documentation for config structure

## Future Schemas

### Agent Schema
Validate agent definition files:
```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "type": "object",
  "properties": {
    "name": { "type": "string" },
    "description": { "type": "string" },
    "tools": { "type": "array", "items": { "type": "string" } }
  },
  "required": ["name", "description"]
}
```

### Skill Schema
Validate skill definition files.

### Command Schema
Validate command definition files.

## How to Use

1. Create schema JSON file
2. Reference in your config file:
```json
{
  "$schema": "./schemas/agent.schema.json",
  "name": "my-agent",
  ...
}
```

## Reference

See JSON Schema documentation:
https://json-schema.org/
