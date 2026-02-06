# WIT Backend Project

Spring Boot 기반의 여행 동행 매칭 서비스 백엔드입니다.

## Tech Stack

- Java 21, Spring Boot 3.x, Spring Modulith
- PostgreSQL, JPA, QueryDSL
- Gradle, Flyway

## Project Structure

```
com.wit
├── common/        # 공통 모듈 (예외, 응답, 설정)
├── infra/         # 인프라 설정 (JPA, Redis, Security)
├── user/          # 사용자 관리
├── authentication/# 인증
├── authorization/ # 인가
├── feed/          # 피드
├── companion/     # 동행
├── chat/          # 채팅
├── home/          # 홈 피드
└── notification/  # 알림
```

---

## CRITICAL: Skill Loading Rules

**코드를 작성하거나 수정하기 전에 반드시 관련 스킬을 로드하세요.**

### 스킬 구조

```
.opencode/skills/
├── testing/               # 테스트/품질
│   ├── tdd/
│   └── springboot-tdd/
├── code-quality/          # 코드 품질
│   ├── coding-convention/
│   ├── logging/
│   └── interface-design/
├── architecture/          # 아키텍처
│   ├── spring-modulith/
│   └── transaction-boundary/
├── spring-boot/           # Spring Boot 특화
│   ├── springboot-patterns/
│   ├── jpa-patterns/
│   └── connection-management/
├── security/              # 보안 (TODO)
└── operations/            # 운영 (TODO)
```

### 필수 스킬 로드 규칙

| 작업 유형 | 로드할 스킬 |
|----------|------------|
| 모든 코드 작성 | `code-quality/coding-convention` |
| 모듈 기능 구현 | `coding-convention` + `architecture/spring-modulith` |
| TDD 방식 개발 | `coding-convention` + `testing/tdd` |
| JPA/DB 작업 | `spring-boot/jpa-patterns` + `architecture/transaction-boundary` |
| 로깅 추가 | `code-quality/logging` |
| 인터페이스 설계 | `code-quality/interface-design` |

### 스킬 로드 방법

```
skill({ name: "coding-convention" })
skill({ name: "spring-modulith" })
skill({ name: "tdd" })
skill({ name: "jpa-patterns" })
skill({ name: "logging" })
```

**스킬을 로드하지 않고 코드를 작성하면 컨벤션 위반이 발생할 수 있습니다.**

---

## Available Agents

| Agent | Purpose | Command | When to Use |
|-------|---------|---------|-------------|
| `planner` | 구현 계획 수립 | `/plan` | 복잡한 기능, 다단계 작업 |
| `architect` | 아키텍처 설계 | `/arch-review` | 새 모듈, 아키텍처 결정 |
| `tdd-guide` | TDD 방식 개발 | `/tdd` | 새 기능, 버그 수정 |
| `code-reviewer` | 코드 품질 검토 | `/code-review` | 코드 작성/수정 후 |
| `security-reviewer` | 보안 취약점 검토 | `/security` | 민감한 코드, 커밋 전 |
| `database-reviewer` | DB/JPA 최적화 | `/db-review` | N+1 문제, 쿼리 최적화 |
| `build-error-resolver` | 빌드 오류 해결 | `/build-fix` | 빌드 실패 시 |
| `refactor-cleaner` | 리팩토링/정리 | `/refactor-clean` | 코드 정리 |

### Agent 자동 사용 규칙

다음 상황에서는 사용자 요청 없이도 Agent를 자동으로 사용:

1. **복잡한 기능 요청** → `planner` 먼저 사용
2. **코드 작성/수정 완료** → `code-reviewer` 사용
3. **버그 수정/새 기능** → `tdd-guide` 사용
4. **아키텍처 결정** → `architect` 사용
5. **빌드 실패** → `build-error-resolver` 사용
6. **DB 변경** → `database-reviewer` 사용

---

## Development Workflow

### Issue-Driven Development

1. **GitHub 이슈 먼저 생성** (`gh issue create`)
2. 이슈 번호로 브랜치 생성: `feat/#123-user-signup`
3. 작업 진행 (스킬 로드 필수!)
4. PR 생성 시 이슈 번호 연결: `Closes #123`

### Commit Convention

```
<type>(<scope>): <subject>
```

Types: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`, `perf`
Scope: 모듈명 (`user`, `feed`, `companion`, `chat`, `notification`, `auth`, `common`, `config`)

### Commit Habits

**작은 단위로 자주 커밋:**
- Entity 생성 후 → 커밋
- Repository 생성 후 → 커밋
- Service 메서드 구현 후 → 커밋
- 테스트 작성 후 → 커밋
- Controller 구현 후 → 커밋

---

## Code Quality Rules

### Transaction Management
- 트랜잭션 범위는 최소화
- 외부 API 호출은 트랜잭션 밖에서
- 읽기 전용은 `@Transactional(readOnly = true)`
- 상세: `architecture/transaction-boundary` 스킬 참조

### DB Connection Management
- 커넥션 점유 시간 최소화
- Open Session In View 비활성화 권장
- Lazy Loading 주의 (트랜잭션 내에서만)
- 상세: `spring-boot/connection-management` 스킬 참조

### Interface Design (DIP)
- 외부 서비스는 인터페이스로 추상화
- Service Layer는 Interface + Impl 패턴
- 상세: `code-quality/interface-design` 스킬 참조

### Logging
- 구조화된 로그 (key=value 형식)
- 적절한 로그 레벨 사용
- 민감정보 로깅 금지
- 상세: `code-quality/logging` 스킬 참조

---

## Security Rules

### Mandatory Checks
- No hardcoded secrets in source code
- All API endpoints must have authentication (except public endpoints)
- Input validation on all request DTOs
- SQL injection prevention (use JPA/QueryDSL only)

### Secrets Management
```java
// NEVER do this
String apiKey = "sk_live_abc123";

// Use environment variables
@Value("${jwt.secret}")
private String jwtSecret;
```

---

## Autonomous Actions

### Proceed Without Asking
- Following established conventions (after loading skills)
- Creating/modifying files (reversible via git)
- Running tests, builds, formatting
- Implementing CRUD following patterns
- Writing unit/integration tests
- Using agents proactively (code-review after coding, etc.)

### Ask Before Proceeding
- Deleting files or directories
- Dropping database tables
- Multiple valid approaches exist
- Unclear requirements
- Significant architectural decisions
- Pushing to remote, deploying

---

## Database

### Flyway Migration Naming
```
V<version>__<action>__<target>__<description>.sql
```

Examples:
- `V1__init.sql`
- `V2__create__survey_table.sql`
- `V3__add__users__phone_column.sql`

### Table/Column Naming
- Tables: plural lowercase snake_case (`users`, `chat_rooms`)
- Columns: lowercase snake_case (`user_id`, `created_at`)
- Primary key: `id`
- Foreign keys: `<table_singular>_id`

---

## .opencode/ Structure

```
.opencode/
├── agents/           # AI 에이전트 정의
├── commands/         # 슬래시 명령어
├── skills/           # 재사용 가능한 스킬
├── rules/            # 전역 규칙
├── hooks/            # 이벤트 트리거 (TODO)
├── mcp-configs/      # MCP 서버 설정 (TODO)
├── schemas/          # 스키마 정의 (TODO)
└── opencode.json     # 메인 설정
```

전체 규칙 상세: `.opencode/rules/` 참조
