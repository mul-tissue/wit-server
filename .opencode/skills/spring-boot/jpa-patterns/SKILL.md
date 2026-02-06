---
name: jpa-patterns
description: JPA/Hibernate patterns. N+1 prevention, transactions, indexing.
---

# JPA Patterns

## Entity Design

```java
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email"),
    @Index(name = "idx_user_status_created", columnList = "status, created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Enumerated(EnumType.STRING)
    private UserStatus status;
    
    @CreatedDate
    private Instant createdAt;
    
    // Business method (NO SETTER)
    public void activate() {
        if (this.status != UserStatus.PENDING) {
            throw new IllegalStateException("Cannot activate");
        }
        this.status = UserStatus.ACTIVE;
    }
}
```

## N+1 Prevention

```java
// BAD: N+1 query
List<User> users = userRepository.findAll();
users.forEach(u -> u.getPosts().size()); // N additional queries!

// GOOD: Fetch join
@Query("SELECT u FROM User u LEFT JOIN FETCH u.posts")
List<User> findAllWithPosts();

// GOOD: DTO projection
@Query("SELECT new UserDto(u.id, u.name) FROM User u")
List<UserDto> findAllSummaries();
```

## Transactions

```java
// Command service
@Service
public class UserServiceImpl {
    @Transactional
    public void create(CreateUserRequest req) { }
}

// Query service
@Service
@Transactional(readOnly = true)
public class UserQueryServiceImpl {
    public UserDto findById(Long id) { }
}
```

## Repository Patterns

```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.status = :status")
    Page<User> findByStatus(@Param("status") UserStatus status, Pageable pageable);
}
```

## Connection Pool (HikariCP)

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
      leak-detection-threshold: 60000
```

## Checklist

- [ ] No N+1 patterns (use fetch join or DTO projection)
- [ ] `readOnly = true` for queries
- [ ] Indexes on filtered columns
- [ ] No lazy loading outside transaction
- [ ] Business methods instead of setters
