---
name: tdd
description: TDD workflow and test patterns. Load when writing tests.
---

# TDD Rules

## Workflow: Red-Green-Refactor

1. **RED**: Write failing test
2. **GREEN**: Minimal code to pass
3. **REFACTOR**: Improve code (tests still pass)

## Test Structure (MANDATORY)

```java
@Test
void shouldExpectedBehavior_whenCondition() {
    // given - setup data and mocks
    
    // when - execute ONE method
    
    // then - verify results
}
```

## Test Types

### Unit Test
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock UserRepository userRepository;
    @InjectMocks UserServiceImpl userService;
    
    @Test
    void shouldCreateUser_whenValidRequest() {
        // given
        when(userRepository.save(any())).thenReturn(user);
        // when
        var result = userService.create(request);
        // then
        assertThat(result.id()).isNotNull();
        verify(userRepository).save(any());
    }
}
```

### Integration Test
```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class UserControllerTest {
    @Autowired MockMvc mockMvc;
    
    @Test
    void shouldCreateUser() throws Exception {
        mockMvc.perform(post("/api/users")
                .contentType(APPLICATION_JSON)
                .content(json))
            .andExpect(status().isCreated());
    }
}
```

### Repository Test
```java
@DataJpaTest
class UserRepositoryTest {
    @Autowired UserRepository repo;
    
    @Test
    void shouldFindByEmail() {
        // given
        repo.save(user);
        // when
        var found = repo.findByEmail("test@example.com");
        // then
        assertThat(found).isPresent();
    }
}
```

## Coverage Targets

| Layer | Target |
|-------|--------|
| Service | 80%+ |
| Domain (business methods) | 90%+ |
| Repository (custom) | 70%+ |

## Commands

```bash
./gradlew test
./gradlew test jacocoTestReport
```
