---
name: interface-design
description: When and how to use interfaces (DIP).
---

# Interface Design (DIP)

## When to Use Interface

### ALWAYS Use For:

**1. External Services**
```java
public interface PaymentGateway {
    PaymentResult process(PaymentRequest request);
}

@Component
@Profile("prod")
class KakaoPayGateway implements PaymentGateway { }

@Component
@Profile("test")
class MockPaymentGateway implements PaymentGateway { }
```

**2. Services (CQRS)**
```java
public interface UserService {
    UserResponse create(CreateUserRequest req);
    void update(Long id, UpdateUserRequest req);
}

public interface UserQueryService {
    UserResponse findById(Long id);
    Page<UserResponse> findAll(Pageable pageable);
}
```

**3. Multiple Implementations**
```java
public interface NotificationSender {
    void send(Notification notification);
}

@Component("email") class EmailSender implements NotificationSender { }
@Component("sms") class SmsSender implements NotificationSender { }
```

### DON'T Use For:

```java
// Single implementation that won't change → No interface
@Component
public class UserValidator { }

// Repository → Spring Data already provides interface
public interface UserRepository extends JpaRepository<User, Long> { }
```

## Naming

```java
// Interface: What it does
public interface PaymentGateway { }
public interface UserService { }

// Implementation: How it does it
class KakaoPayGateway implements PaymentGateway { }
class UserServiceImpl implements UserService { }
```

## Checklist

Ask before creating:
- [ ] Multiple implementations?
- [ ] External system integration?
- [ ] Needs easy mocking?
- [ ] Implementation likely to change?

YES to any → Use interface
NO to all → Direct class (YAGNI)
