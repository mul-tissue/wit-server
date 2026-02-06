---
name: connection-management
description: DB connection management. Keep connections short.
---

# DB Connection Management

## Core Rule

**Use connections briefly, return them quickly.**

## HikariCP Config

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000
```

## Anti-Patterns

### External Call Inside Transaction

```java
// BAD: HTTP call holds connection
@Transactional
public void process() {
    save();
    externalApi.call();  // Connection held during HTTP!
    update();
}

// GOOD: External call outside transaction
public void process() {
    createPending();         // Short transaction
    externalApi.call();      // No connection held
    complete();              // Short transaction
}
```

### Lazy Loading Outside Transaction

```java
// BAD: OSIV holds connection in controller
User user = userService.getUser(id);
user.getPosts().size();  // New query in controller!

// GOOD: Fetch in service, return DTO
@Transactional(readOnly = true)
public UserDto getUser(Long id) {
    User user = userRepository.findWithPostsById(id);
    return UserDto.from(user);
}
```

## Disable OSIV

```yaml
spring:
  jpa:
    open-in-view: false
```

## Checklist

- [ ] OSIV disabled
- [ ] No external calls inside transaction
- [ ] No file I/O inside transaction
- [ ] Leak detection enabled
- [ ] Batch operations chunked
