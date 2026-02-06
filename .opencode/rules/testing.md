# Testing Rules

## Core Principles

1. **Write tests BEFORE implementation** (TDD)
2. **Every feature must have tests**
3. **Tests must be deterministic** (no flaky tests)
4. **Tests must be independent** (no shared state)

## Test Coverage Requirements

| Layer | Minimum | Target |
|-------|---------|--------|
| Service (business logic) | 70% | 80% |
| Domain (business methods) | 80% | 90% |
| Repository (custom queries) | 60% | 70% |
| Overall | 50% | 60% |

## Test Structure (Given-When-Then)

**All tests MUST follow Given-When-Then structure:**

```java
@Test
@DisplayName("유효한 요청으로 사용자 생성 시 사용자가 반환된다")
void shouldCreateUser_whenValidRequest() {
    // given - setup
    CreateUserRequest request = new CreateUserRequest("test@example.com", "John");
    
    // when - execute
    UserResponse response = userService.create(request);
    
    // then - verify
    assertThat(response.id()).isNotNull();
    assertThat(response.email()).isEqualTo("test@example.com");
}
```

## Test Naming Convention

```java
// Pattern: should<ExpectedBehavior>_when<Condition>
void shouldReturnUser_whenIdExists() { }
void shouldThrowException_whenUserNotFound() { }
void shouldPublishEvent_whenUserCreated() { }
```

## Test Types

### Unit Tests (Required)
- Test individual methods in isolation
- Mock all external dependencies
- Fast execution (< 100ms per test)

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock UserRepository userRepository;
    @InjectMocks UserServiceImpl userService;
}
```

### Integration Tests (Required for APIs)
- Test entire request-response cycle
- Use real database (H2 or Testcontainers)
- Test API endpoints

```java
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {
    @Autowired MockMvc mockMvc;
}
```

### Repository Tests (Required for custom queries)
```java
@DataJpaTest
class UserRepositoryTest {
    @Autowired UserRepository userRepository;
}
```

## What to Test

### MUST Test
- Business logic in services
- State transitions in domain entities
- Custom repository queries
- API endpoint responses
- Error handling paths
- Validation logic

### Don't Test
- Simple getters/setters
- DTOs with no logic
- Framework code
- Configuration classes

## Assertions

Use AssertJ for all assertions:

```java
// GOOD: AssertJ
assertThat(user.getEmail()).isEqualTo("test@example.com");
assertThat(list).hasSize(3).contains(user1, user2);
assertThatThrownBy(() -> service.findById(999L))
    .isInstanceOf(BusinessException.class);

// AVOID: JUnit assertions
assertEquals("test@example.com", user.getEmail());
```

## Mocking Guidelines

### Mock External Dependencies
```java
@Mock PaymentGateway paymentGateway;
@Mock EmailService emailService;
```

### Don't Mock
- Value objects
- DTOs
- Domain entities (usually)

### Verify Interactions
```java
verify(userRepository).save(any(User.class));
verify(eventPublisher).publishEvent(any(UserCreatedEvent.class));
verifyNoMoreInteractions(userRepository);
```

## Test Data

### Use Builders/Fixtures
```java
User user = UserFixture.builder()
    .withEmail("test@example.com")
    .withStatus(UserStatus.ACTIVE)
    .build();
```

### Avoid Magic Values
```java
// BAD
User user = new User(1L, "test", "test@test.com");

// GOOD
Long userId = 1L;
String email = "test@example.com";
User user = User.builder().id(userId).email(email).build();
```

## CI Integration

### Before Merge
- All tests must pass
- Coverage must meet thresholds
- No skipped tests

### Commands
```bash
# Run all tests
./gradlew test

# Run with coverage
./gradlew test jacocoTestReport

# View report
open build/reports/jacoco/test/html/index.html
```

## Anti-Patterns to Avoid

- ❌ Tests depending on execution order
- ❌ Tests sharing mutable state
- ❌ Testing implementation details
- ❌ Ignoring failing tests with @Disabled
- ❌ Tests with no assertions
- ❌ Sleeping in tests (use await)
- ❌ Testing trivial code

## Test Checklist

Before committing:
- [ ] All tests pass locally
- [ ] New code has tests
- [ ] Given-When-Then structure used
- [ ] Edge cases covered
- [ ] No flaky tests
- [ ] Tests are independent
- [ ] Coverage meets requirements
