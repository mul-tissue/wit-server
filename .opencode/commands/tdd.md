---
description: Enforce TDD workflow with test-first development
agent: tdd-guide
subtask: true
---

# TDD Command

Implement the following using strict test-driven development: $ARGUMENTS

## TDD Cycle (MANDATORY)

```
RED -> GREEN -> REFACTOR -> REPEAT
```

1. **RED**: Write a failing test FIRST
2. **GREEN**: Write minimal code to pass the test
3. **REFACTOR**: Improve code while keeping tests green
4. **REPEAT**: Continue until feature complete

## Your Task

### Step 1: Load Skills
```
skill({ name: "coding-convention" })
skill({ name: "tdd" })
```

### Step 2: Define Interface (SCAFFOLD)
- Define method signature in Service/Repository
- Create Request/Response DTOs

### Step 3: Write Failing Tests (RED)

**Unit Test Example:**
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    @DisplayName("given valid request when createUser then returns created user")
    void createUser_ValidRequest_ReturnsCreatedUser() {
        // given
        var request = new CreateUserRequest("test@email.com", "password");
        var expected = User.create(request.email(), request.password());
        given(userRepository.save(any())).willReturn(expected);
        
        // when
        var result = userService.createUser(request);
        
        // then
        assertThat(result.email()).isEqualTo("test@email.com");
    }
}
```

### Step 4: Implement Minimal Code (GREEN)
- Write just enough code to make tests pass
- No premature optimization

### Step 5: Refactor (IMPROVE)
- Extract constants, improve naming
- Remove duplication
- Keep tests passing

### Step 6: Run All Tests
```bash
./gradlew test
```

## Test Coverage Requirements

| Code Type | Minimum |
|-----------|---------|
| Service layer | 80% |
| Domain logic | 90% |
| Controller | Integration tests |
| Repository | Integration tests |

## Test Naming Convention

```
methodName_StateUnderTest_ExpectedBehavior
```

Examples:
- `createUser_ValidRequest_ReturnsCreatedUser`
- `findById_NonExistentId_ThrowsNotFoundException`
- `update_UnauthorizedUser_ThrowsForbiddenException`

---

**MANDATORY**: Tests must be written BEFORE implementation. Never skip the RED phase.
