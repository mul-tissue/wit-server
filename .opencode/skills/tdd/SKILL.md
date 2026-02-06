---
name: tdd
description: Test-driven development rules including Red-Green-Refactor workflow, Given-When-Then test structure, coverage requirements, and testing patterns for Spring Boot applications. Load when doing TDD or writing tests.
---

# TDD Rules

## Mandatory Requirements
1. **Write tests BEFORE implementation**
2. **Minimum 70% code coverage** (measure with JaCoCo)
3. **All API endpoints must have integration tests**

---

## TDD Workflow (Red-Green-Refactor)

### 1. RED: Write a failing test
```java
@Test
void shouldCreateUser_whenValidRequest() {
    // Given
    UserRequest request = new UserRequest("test@example.com", "John", "password123");
    
    // When & Then
    assertThatThrownBy(() -> userService.create(request))
        .isInstanceOf(RuntimeException.class); // Will fail because not implemented yet
}
```

### 2. GREEN: Write minimal code to pass
```java
@Service
public class UserService {
    public UserResponse create(UserRequest request) {
        // Minimal implementation
        return new UserResponse(1L, request.email(), request.name(), LocalDateTime.now());
    }
}
```

### 3. REFACTOR: Improve code quality
```java
@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    public UserResponse create(UserRequest request) {
        User user = User.builder()
            .email(request.email())
            .name(request.name())
            .build();
        
        User saved = userRepository.save(user);
        eventPublisher.publishEvent(new UserCreatedEvent(saved.getId()));
        
        return UserResponse.from(saved);
    }
}
```

---

## Test Structure (Given-When-Then) - MANDATORY

**All tests must follow Given-When-Then structure.**

```java
@Test
@DisplayName("유효한 요청으로 사용자 생성 시 사용자가 반환된다")
void create_ValidRequest_ReturnsUser() {
    // given - test data and mock setup
    UserCreateRequest request = new UserCreateRequest("test@example.com", "John");
    User savedUser = User.builder().id(1L).email(request.email()).build();
    given(userRepository.save(any(User.class))).willReturn(savedUser);
    
    // when - execute method under test
    UserResponse response = userService.create(request);
    
    // then - verify results
    assertThat(response.id()).isEqualTo(1L);
    assertThat(response.email()).isEqualTo("test@example.com");
    then(userRepository).should().save(any(User.class));
}
```

### Given-When-Then Rules
- **given**: Set up preconditions (data, mocks)
- **when**: Execute target method **exactly once**
- **then**: Verify results (assertThat, verify)
- Comments `// given`, `// when`, `// then` are **required**

---

## Test Types

### Unit Tests
- Test individual methods in isolation
- Mock external dependencies
- Fast execution

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private ApplicationEventPublisher eventPublisher;
    
    @InjectMocks
    private UserServiceImpl userService;
    
    @Test
    void shouldCreateUser_whenValidRequest() {
        // given
        UserRequest request = new UserRequest("test@example.com", "John", "password123");
        User savedUser = User.builder().id(1L).email(request.email()).build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        // when
        UserResponse response = userService.create(request);
        
        // then
        assertThat(response.id()).isEqualTo(1L);
        verify(userRepository).save(any(User.class));
        verify(eventPublisher).publishEvent(any(UserCreatedEvent.class));
    }
}
```

### Integration Tests
- Test entire request-response cycle
- Use real database (H2 or Testcontainers)
- Test API endpoints

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class UserControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void shouldCreateUser_whenValidRequest() throws Exception {
        // given
        UserRequest request = new UserRequest("test@example.com", "John", "password123");
        
        // when & then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNotNull())
            .andExpect(jsonPath("$.email").value("test@example.com"));
    }
}
```

### Repository Tests
```java
@DataJpaTest
class UserRepositoryTest {
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    void shouldSaveUser() {
        // given
        User user = User.builder()
            .email("test@example.com")
            .name("John")
            .build();
        
        // when
        User saved = userRepository.save(user);
        
        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("test@example.com");
    }
}
```

---

## Testing Guidelines

### What to Test
- Business logic in Services
- API endpoints (integration tests)
- Repository queries (custom queries)
- Validation logic
- Exception handling
- Event publishing and listening

### What NOT to Test
- Simple getters/setters
- Framework code (Spring Boot auto-configuration)
- Third-party libraries
- DTOs with no logic

---

## Test Naming Convention
```java
// Pattern: should<ExpectedBehavior>_when<Condition>
@Test
void shouldReturnUser_whenIdExists() { }

@Test
void shouldThrowException_whenUserNotFound() { }

@Test
void shouldPublishEvent_whenUserCreated() { }
```

---

## Coverage Requirements

### Test Targets (Business Logic Focus)
- **Service Layer**: Core business logic - **Required**
- **QueryService**: Complex query logic - **Required**
- **Domain Entity**: Business methods (state changes) - **Required**
- **Custom Repository**: QueryDSL and custom queries - **Required**

### Excluded from Tests
- **Controller**: Simple delegation (covered by integration tests)
- **DTO**: Records/classes with no logic
- **Config**: Configuration classes
- **Entity getters**: Simple accessors

### Coverage Goals
| Layer | Goal | Note |
|--------|------|------|
| Service | 80%+ | Core business logic |
| Domain (business methods) | 90%+ | State change logic |
| Repository (custom) | 70%+ | Complex queries |
| Overall | 60%+ | OK to be lower excluding business logic |

---

## Testing Spring Modulith Events

```java
@SpringBootTest
class UserEventTest {
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    @MockBean
    private NotificationService notificationService;
    
    @Test
    void shouldTriggerNotification_whenUserCreatedEventPublished() {
        // given
        Long userId = 1L;
        
        // when
        eventPublisher.publishEvent(new UserCreatedEvent(userId));
        
        // then
        verify(notificationService, timeout(1000)).sendWelcomeNotification(userId);
    }
}
```

---

## Test Database Configuration

```yaml
# src/test/resources/application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
```

---

## Assertions Library

Use AssertJ for fluent assertions:
```java
// AssertJ (preferred)
assertThat(user.getEmail()).isEqualTo("test@example.com");
assertThat(list).hasSize(3).contains(user1, user2);

// JUnit assertions (avoid)
assertEquals("test@example.com", user.getEmail());
```

---

## Test Coverage with JaCoCo

```gradle
// build.gradle
plugins {
    id 'jacoco'
}

test {
    finalizedBy jacocoTestReport
}

jacoco {
    toolVersion = "0.8.11"
}

jacocoTestReport {
    dependsOn test
    reports {
        xml.required = true
        html.required = true
    }
}

jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = 0.70
            }
        }
    }
}
```

Run coverage:
```bash
./gradlew test jacocoTestReport
# View report at: build/reports/jacoco/test/html/index.html
```
