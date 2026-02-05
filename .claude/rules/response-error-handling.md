---
name: response-error-handling
description: API Response format and Error handling conventions
---

# Response & Error Handling

## Package Structure

```
com.wit.common
├── exception/
│   ├── code/
│   │   ├── ErrorCode.java        # 에러코드 인터페이스
│   │   └── GlobalErrorCode.java  # 공통 에러코드
│   ├── dto/
│   │   └── ErrorResponse.java    # 에러 응답 DTO
│   ├── CustomException.java      # 커스텀 예외
│   └── handler/
│       └── GlobalExceptionHandler.java
└── entity/
    └── BaseTimeEntity.java       # 공통 Entity
```

## API Response Format

### Success Response
Controller에서 `ResponseEntity<T>`를 직접 반환합니다.

```json
{
  "accessToken": "eyJhbGc...",
  "isNewUser": true,
  "needsOnboarding": true
}
```

### Error Response
GlobalExceptionHandler가 처리하여 `ErrorResponse` 형태로 반환합니다.

```json
{
  "errorName": "USER_NOT_FOUND",
  "message": "사용자를 찾을 수 없습니다."
}
```

## Implementation Pattern

### ErrorResponse (record)
```java
public record ErrorResponse(
    String errorName,
    String message
) {
    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.getErrorName(), errorCode.getMessage());
    }
}
```

### ErrorCode Interface
```java
public interface ErrorCode {
    HttpStatus getHttpStatus();
    String getMessage();
    String getErrorName();  // enum name() 반환
}
```

### Domain별 ErrorCode
각 도메인 모듈에 `exception/` 패키지 생성:

```
com.wit.user.exception/
└── UserErrorCode.java

com.wit.feed.exception/
└── FeedErrorCode.java

com.wit.companion.exception/
└── CompanionErrorCode.java
```

### ErrorCode Enum 예시
```java
@Getter
@AllArgsConstructor
public enum UserErrorCode implements ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    INVALID_USER_STATUS(HttpStatus.BAD_REQUEST, "유효하지 않은 사용자 상태입니다."),
    ;

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public String getErrorName() {
        return this.name();
    }
}
```

### GlobalErrorCode (공통)
```java
@Getter
@AllArgsConstructor
public enum GlobalErrorCode implements ErrorCode {
    // Validation
    METHOD_ARGUMENT_NOT_VALID(HttpStatus.BAD_REQUEST, "요청 값이 유효하지 않습니다."),
    METHOD_ARGUMENT_TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "요청 값의 타입이 올바르지 않습니다."),
    HTTP_MESSAGE_NOT_READABLE(HttpStatus.BAD_REQUEST, "요청 데이터 형식이 올바르지 않습니다."),
    
    // HTTP
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP method입니다."),
    
    // Server
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public String getErrorName() {
        return this.name();
    }
}
```

### CustomException
```java
@Getter
public class CustomException extends RuntimeException {
    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
```

### 예외 던지기
```java
// Service에서 예외 발생
public User findById(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
}
```

## GlobalExceptionHandler

### 처리 순서
1. `CustomException` - 비즈니스 예외
2. `MethodArgumentNotValidException` - @Valid 검증 실패
3. `MethodArgumentTypeMismatchException` - 타입 불일치
4. `HttpMessageNotReadableException` - JSON 파싱 오류
5. `HttpRequestMethodNotSupportedException` - HTTP 메서드 오류
6. `RuntimeException` - 예상치 못한 런타임 예외
7. `Exception` - 기타 모든 예외

### 로깅 규칙
- `CustomException`: WARN 레벨
- `RuntimeException`, `Exception`: ERROR 레벨 + 스택트레이스

## Controller에서 사용

```java
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
        UserResponse response = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        UserResponse response = userQueryService.findById(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

## 주의사항

### ❌ 하지 말 것
- Service에서 `ResponseEntity` 반환하지 않기 (Controller만)
- 여러 모듈에서 같은 ErrorCode 정의하지 않기
- catch로 예외 삼키지 않기 (로깅 후 재던지기)

### ✅ 할 것
- Controller는 항상 `ResponseEntity<T>` 반환
- 비즈니스 예외는 항상 `CustomException` + `ErrorCode` 사용
- ErrorCode 메시지는 사용자에게 보여줄 수 있는 한국어로
- 도메인별로 ErrorCode enum 분리
- HTTP 상태 코드는 `ResponseEntity`와 `ErrorCode`의 `HttpStatus`로 관리
