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

### 필수 스킬 로드 규칙

| 작업 유형 | 로드할 스킬 |
|----------|------------|
| 모든 코드 작성 | `coding-convention` |
| 모듈 기능 구현 | `coding-convention` + `spring-modulith` |
| TDD 방식 개발 | `coding-convention` + `tdd` |
| 새 모듈 생성 | `coding-convention` + `spring-modulith` |

### 스킬 로드 방법

```
skill({ name: "coding-convention" })
skill({ name: "spring-modulith" })
skill({ name: "tdd" })
```

**스킬을 로드하지 않고 코드를 작성하면 컨벤션 위반이 발생할 수 있습니다.**

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

Examples:
```
feat(user): add user registration API
fix(auth): resolve JWT token expiration issue
refactor(feed): extract query logic to QueryService
test(companion): add integration tests for join flow
```

### Commit Habits

**작은 단위로 자주 커밋:**
- Entity 생성 후 → 커밋
- Repository 생성 후 → 커밋
- Service 메서드 구현 후 → 커밋
- 테스트 작성 후 → 커밋
- Controller 구현 후 → 커밋

### PR Workflow

1. 이슈 번호 확인 (없으면 생성)
2. 모든 테스트 통과
3. Spotless 포맷팅 적용
4. PR 생성 (`Closes #123` 포함)

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

## Available Agents

| Agent | Purpose | Command |
|-------|---------|---------|
| `planner` | 복잡한 기능 설계 | `/plan` |
| `tdd-guide` | TDD 방식 구현 | `/tdd` |
| `code-reviewer` | 코드 품질 검토 | `/code-review` |

### When to Use Agents

- `/plan`: 여러 모듈에 걸친 복잡한 기능 설계
- `/tdd`: TDD 방식으로 기능 구현
- `/code-review`: 기능 구현 완료 후 PR 전
