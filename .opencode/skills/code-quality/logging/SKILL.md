---
name: logging
description: Structured logging patterns. key=value format.
---

# Logging Patterns

## Log Levels

| Level | Use | Example |
|-------|-----|---------|
| ERROR | Immediate action needed | DB failed, payment failed |
| WARN | Potential issue | Retry attempt, deprecated API |
| INFO | Business events | User created, order completed |
| DEBUG | Development | Method entry, variable values |

## Structured Format

```java
// Pattern: action=<name> key=value key=value
log.info("action=create_order userId={} items={}", userId, itemCount);
log.info("action=order_completed orderId={} total={} durationMs={}", id, total, ms);
log.error("action=payment_failed orderId={} reason={}", id, e.getMessage(), e);
```

## Examples

```java
@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    
    public Order create(CreateOrderRequest req) {
        log.info("action=create_order userId={}", req.getUserId());
        
        try {
            Order order = process(req);
            log.info("action=create_order_success orderId={} total={}", 
                order.getId(), order.getTotal());
            return order;
        } catch (Exception e) {
            log.error("action=create_order_failed userId={} reason={}", 
                req.getUserId(), e.getMessage(), e);
            throw e;
        }
    }
}
```

## DO NOT Log

- Passwords (even hashed)
- JWT tokens
- API keys
- Credit card numbers
- Full session IDs

## Mask Sensitive Data

```java
String maskedEmail = email.replaceAll("(.).*@", "$1***@");
String maskedCard = "**** **** **** " + card.substring(card.length() - 4);
```

## Checklist

- [ ] Use `action=` prefix
- [ ] key=value format
- [ ] Include IDs for correlation
- [ ] Appropriate level
- [ ] No sensitive data
