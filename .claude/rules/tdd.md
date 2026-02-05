---
name: tdd
description: Test-driven development rules and practices
---

# TDD Rules

## Mandatory Requirements
1. **Write tests BEFORE implementation**
2. **Minimum 70% code coverage** (measure with JaCoCo)
3. **All API endpoints must have integration tests**

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

## Test Structure (Given-When-Then) - 필수!

**모든 테스트는 반드시 Given-When-Then 구조를 따라야 합니다.**

```java
@Test
@DisplayName("유효한 요청으로 사용자 생성 시 사용자가 반환된다")
void create_ValidRequest_ReturnsUser() {
    // given - 테스트 데이터 및 Mock 설정
    UserCreateRequest request = new UserCreateRequest("test@example.com", "John");
    User savedUser = User.builder().id(1L).email(request.email()).build();
    given(userRepository.save(any(User.class))).willReturn(savedUser);
    
    // when - 테스트 대상 메서드 실행
    UserResponse response = userService.create(request);
    
    // then - 결과 검증
    assertThat(response.id()).isEqualTo(1L);
    assertThat(response.email()).isEqualTo("test@example.com");
    then(userRepository).should().save(any(User.class));
}
```

### Given-When-Then 규칙
- **given**: 테스트 전제 조건 설정 (데이터, Mock)
- **when**: 테스트 대상 메서드 **단 하나만** 실행
- **then**: 결과 검증 (assertThat, verify)
- 주석으로 `// given`, `// when`, `// then` 반드시 표시

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
    private UserService userService;
    
    @Test
    void shouldCreateUser_whenValidRequest() {
        // Given
        UserRequest request = new UserRequest("test@example.com", "John", "password123");
        User savedUser = User.builder().id(1L).email(request.email()).build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        // When
        UserResponse response = userService.create(request);
        
        // Then
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
        // Given
        UserRequest request = new UserRequest("test@example.com", "John", "password123");
        
        // When & Then
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
        // Given
        User user = User.builder()
            .email("test@example.com")
            .name("John")
            .build();
        
        // When
        User saved = userRepository.save(user);
        
        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("test@example.com");
    }
}
```

## Testing Guidelines

### What to Test
- ✅ Business logic in Services
- ✅ API endpoints (integration tests)
- ✅ Repository queries (custom queries)
- ✅ Validation logic
- ✅ Exception handling
- ✅ Event publishing and listening

### What NOT to Test
- ❌ Simple getters/setters
- ❌ Framework code (Spring Boot auto-configuration)
- ❌ Third-party libraries
- ❌ DTOs with no logic

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

## Coverage Requirements

### 테스트 대상 (비즈니스 로직 중심)
- **Service 레이어**: 핵심 비즈니스 로직 → 필수 테스트
- **QueryService**: 복잡한 조회 로직 → 필수 테스트
- **Domain Entity**: 비즈니스 메서드 (상태 변경 등) → 필수 테스트
- **Custom Repository**: QueryDSL 등 커스텀 쿼리 → 필수 테스트

### 테스트 제외 대상
- **Controller**: 단순 위임만 하는 경우 (통합 테스트로 대체)
- **DTO**: 로직 없는 record/class
- **Config**: 설정 클래스
- **Entity getter**: 단순 조회

### 커버리지 목표
| 레이어 | 목표 | 비고 |
|--------|------|------|
| Service | 80%+ | 비즈니스 로직 핵심 |
| Domain (비즈니스 메서드) | 90%+ | 상태 변경 로직 |
| Repository (커스텀) | 70%+ | 복잡한 쿼리 |
| 전체 | 60%+ | 비즈니스 로직 제외 시 낮아도 OK |

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
        // Given
        Long userId = 1L;
        
        // When
        eventPublisher.publishEvent(new UserCreatedEvent(userId));
        
        // Then
        verify(notificationService, timeout(1000)).sendWelcomeNotification(userId);
    }
}
```

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

## Assertions Library
Use AssertJ for fluent assertions:
```java
// ✅ AssertJ (preferred)
assertThat(user.getEmail()).isEqualTo("test@example.com");
assertThat(list).hasSize(3).contains(user1, user2);

// ❌ JUnit assertions (avoid)
assertEquals("test@example.com", user.getEmail());
```

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
