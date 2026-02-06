# MCP Configurations

Model Context Protocol (MCP) server configurations for external tool integration.

## What is MCP?

MCP allows Claude Code to connect to external tools and services:
- Database query tools
- API testing tools
- Custom automation scripts
- Third-party service integrations

## How to Add MCP Servers

### 1. Create Configuration File

Create a JSON file with your MCP server configuration:

```json
{
  "name": "my-mcp-server",
  "type": "stdio",
  "command": "node",
  "args": ["path/to/server.js"],
  "env": {
    "API_KEY": "${MY_API_KEY}"
  }
}
```

### 2. Register in OpenCode

Add the MCP server to your `opencode.json`:

```json
{
  "mcpServers": {
    "my-server": {
      "command": "node",
      "args": ["path/to/server.js"]
    }
  }
}
```

## Useful MCP Servers to Consider

### Database Tools
- Query execution
- Schema inspection
- Migration tools

### API Testing
- REST client
- GraphQL client
- Mock servers

### External Services
- GitHub enhanced (beyond gh CLI)
- Jira integration
- Slack notifications

## Reference

See MCP documentation:
https://modelcontextprotocol.io/

See everything-claude-code examples:
`/Users/ohhyungsuh/Documents/everything-claude-code/mcp-configs/`
