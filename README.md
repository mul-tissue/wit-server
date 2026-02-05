# Wit Backend

Spring Boot 기반 여행 동행 플랫폼 백엔드 서버

## 아키텍처

- **Modular Monolith** (Spring Modulith)
- **CQRS** (Service 레벨 분리)
- **Event-Driven Architecture**

자세한 내용은 [아키텍처 문서](.claude/rules/architecture.md)를 참고하세요.

## 기술 스택

- Java 21
- Spring Boot 4.0.2
- Spring Modulith
- Spring Data JPA + QueryDSL
- PostgreSQL
- Redis
- JWT
- WebSocket
- Gradle

## 프로젝트 구조

```
com.wit
├── user                # 회원 관리
├── authentication      # 인증 (로그인, 토큰)
├── authorization       # 인가 (JWT 검증)
├── feed                # 피드 (지도 기반)
├── companion           # 동행 모집
├── chat                # 실시간 채팅
├── home                # 홈 화면
├── notification        # 알림
└── common              # 공통 유틸리티
```

각 모듈은 다음 구조를 따릅니다:
```
module/
├── api/          # Controllers
├── service/      # Business logic (Command/Query)
├── domain/       # Entities
├── repository/   # Data access (JPA + QueryDSL)
├── event/        # Domain events
└── dto/          # Request/Response DTOs
```

## 모듈 간 통신

### 조회 (Read)
- 타 모듈의 `QueryService` 호출
- DTO 반환

### 변경 (CUD)
- 자기 모듈만 변경
- Domain Event 발행
- 타 모듈은 Event Listener로 반응

## 개발 규칙

프로젝트는 다음 규칙을 따릅니다:

- [커밋 컨벤션](.claude/rules/commit-convention.md)
- [코딩 스타일](.claude/rules/coding-style.md)
- [TDD 규칙](.claude/rules/tdd.md)
- [보안 규칙](.claude/rules/security.md)

## 빌드 및 실행

```bash
# 빌드
./gradlew build

# 테스트
./gradlew test

# 테스트 커버리지
./gradlew test jacocoTestReport

# 실행
./gradlew bootRun
```

## 환경 변수

```bash
# JWT
export JWT_SECRET=your_jwt_secret

# Database
export DB_URL=jdbc:postgresql://localhost:5432/wit
export DB_USERNAME=postgres
export DB_PASSWORD=your_password

# Redis
export REDIS_HOST=localhost
export REDIS_PORT=6379
```

## 테스트 커버리지 목표

- 전체: 70%+
- Service 레이어: 80%+
- Controller: 100% (통합 테스트)

## 코드 포맷팅

프로젝트는 Spotless를 사용하여 Google Java Format (AOSP 스타일)을 적용합니다.

```bash
# 포맷 검사
./gradlew spotlessCheck

# 자동 포맷팅
./gradlew spotlessApply
```

## AI 에이전트 설정

이 프로젝트는 OpenCode/Claude Code를 위한 설정이 포함되어 있습니다:

- `.claude/rules/`: 항상 따라야 할 규칙
- `.claude/skills/`: 워크플로우 및 패턴 정의

자세한 내용은 [Spring Modulith Skill](.claude/skills/spring-modulith/SKILL.md)을 참고하세요.

## 핵심 원칙

1. **모듈 경계는 코드 규칙으로 지킨다**
2. **다른 도메인의 데이터는 조회 전용으로만 접근한다**
3. **변경은 이벤트로, 조회는 동기 호출로 처리한다**
4. **지금 필요 없는 추상화는 만들지 않는다**

## License

MIT
