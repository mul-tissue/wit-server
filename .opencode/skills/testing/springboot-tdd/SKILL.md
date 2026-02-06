---
name: springboot-tdd
description: Test-driven development for Spring Boot using JUnit 5, Mockito, MockMvc, Testcontainers, and JaCoCo. Use when adding features, fixing bugs, or refactoring.
---

# Spring Boot TDD Workflow

TDD guidance for Spring Boot services with 70%+ coverage (unit + integration).

## When to Use

- New features or endpoints
- Bug fixes or refactors
- Adding data access logic or security rules

## Workflow

1) Write tests first (they should fail)
2) Implement minimal code to pass
3) Refactor with tests green
4) Enforce coverage (JaCoCo)

## Unit Tests (JUnit 5 + Mockito)

```java
@ExtendWith(MockitoExtension.class)
class MarketServiceTest {
  @Mock MarketRepository repo;
  @InjectMocks MarketServiceImpl service;

  @Test
  void createsMarket() {
      // given
      CreateMarketRequest req = new CreateMarketRequest("name", "desc");
      when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

      // when
      Market result = service.create(req);

      // then
      assertThat(result.name()).isEqualTo("name");
      verify(repo).save(any());
  }
}
```

Patterns:
- Arrange-Act-Assert (Given-When-Then)
- Avoid partial mocks; prefer explicit stubbing
- Use `@ParameterizedTest` for variants

## Web Layer Tests (MockMvc)

```java
@WebMvcTest(MarketController.class)
class MarketControllerTest {
  @Autowired MockMvc mockMvc;
  @MockBean MarketService marketService;

  @Test
  void returnsMarkets() throws Exception {
      // given
      when(marketService.list(any())).thenReturn(Page.empty());

      // when & then
      mockMvc.perform(get("/api/markets"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content").isArray());
  }
}
```

## Integration Tests (SpringBootTest)

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MarketIntegrationTest {
  @Autowired MockMvc mockMvc;

  @Test
  void createsMarket() throws Exception {
      // given
      String requestBody = """
          {"name":"Test","description":"Desc"}
          """;

      // when & then
      mockMvc.perform(post("/api/markets")
          .contentType(MediaType.APPLICATION_JSON)
          .content(requestBody))
        .andExpect(status().isCreated());
  }
}
```

## Persistence Tests (DataJpaTest)

```java
@DataJpaTest
class MarketRepositoryTest {
  @Autowired MarketRepository repo;

  @Test
  void savesAndFinds() {
      // given
      MarketEntity entity = new MarketEntity();
      entity.setName("Test");

      // when
      repo.save(entity);
      Optional<MarketEntity> found = repo.findByName("Test");

      // then
      assertThat(found).isPresent();
  }
}
```

## Testcontainers (for Production DB parity)

```java
@SpringBootTest
@Testcontainers
class PostgresIntegrationTest {
  
  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
      .withDatabaseName("testdb");

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
      registry.add("spring.datasource.url", postgres::getJdbcUrl);
      registry.add("spring.datasource.username", postgres::getUsername);
      registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Test
  void testWithRealPostgres() {
      // Test with real PostgreSQL
  }
}
```

## Coverage (JaCoCo)

```gradle
plugins {
    id 'jacoco'
}

test {
    finalizedBy jacocoTestReport
}

jacocoTestReport {
    reports {
        xml.required = true
        html.required = true
    }
}
```

## Assertions

- Prefer AssertJ (`assertThat`) for readability
- For JSON responses, use `jsonPath`
- For exceptions: `assertThatThrownBy(...)`

```java
// AssertJ fluent assertions
assertThat(user.getEmail()).isEqualTo("test@example.com");
assertThat(list).hasSize(3).extracting("name").contains("John");

// Exception assertions
assertThatThrownBy(() -> service.findById(999L))
    .isInstanceOf(BusinessException.class)
    .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_NOT_FOUND);
```

## Test Data Builders

```java
class UserBuilder {
  private String name = "Test User";
  private String email = "test@example.com";
  
  UserBuilder withName(String name) { this.name = name; return this; }
  UserBuilder withEmail(String email) { this.email = email; return this; }
  
  User build() { return new User(null, name, email, UserStatus.ACTIVE); }
}

// Usage
User user = new UserBuilder().withName("John").build();
```

## CI Commands

```bash
# Run tests
./gradlew test

# Run with coverage report
./gradlew test jacocoTestReport

# View HTML report
open build/reports/jacoco/test/html/index.html
```

**Remember**: Keep tests fast, isolated, and deterministic. Test behavior, not implementation details.
