---
name: transaction-boundary
description: Transaction scope management. Keep transactions short.
---

# Transaction Boundary

## Core Rule

**Keep transactions short and focused.**

Long transactions = Long-held connections = Pool exhaustion

## Rules

### 1. No External Calls Inside Transaction

```java
// BAD
@Transactional
public void process() {
    save();
    paymentGateway.charge();  // HTTP call holds connection!
    update();
}

// GOOD
public void process() {
    Long id = createPending();        // ~50ms
    PaymentResult result = charge();  // No connection
    complete(id, result);             // ~50ms
}

@Transactional
Long createPending() { return repo.save(...).getId(); }

@Transactional
void complete(Long id, PaymentResult r) { ... }
```

### 2. Read-Only for Queries

```java
@Service
@Transactional(readOnly = true)  // Default
public class UserQueryService {
    public UserDto findById(Long id) { }
    
    @Transactional  // Override for writes
    public void updateLastLogin(Long id) { }
}
```

### 3. Batch in Chunks

```java
// BAD: All in one transaction
@Transactional
void processAll(List<Order> orders) { orders.forEach(this::process); }

// GOOD: Per-item transaction
void processAll(List<Order> orders) {
    orders.forEach(o -> processOne(o));
}

@Transactional
void processOne(Order order) { process(order); }
```

## Duration Targets

| Operation | Target | Max |
|-----------|--------|-----|
| Simple CRUD | <100ms | 500ms |
| Complex query | <500ms | 2s |

## Checklist

- [ ] No external API calls in transaction
- [ ] No file I/O in transaction
- [ ] `readOnly = true` for queries
- [ ] Transaction scope minimal
- [ ] Batches chunked
