---
name: agents
description: Agent 위임 규칙
---

# Agent Delegation Rules

## 사용 가능한 Agent

| Agent | 용도 | 명령어 |
|-------|------|--------|
| `planner` | 복잡한 기능 설계 | `/plan` |
| `tdd-guide` | TDD 방식 구현 | `/tdd` |
| `code-reviewer` | 코드 품질 검토 | `/code-review` |

## 언제 Agent를 사용하나?

### `/plan` - Planner Agent
```
✅ 사용:
- "동행 매칭 시스템 어떻게 구현할까?"
- "알림 기능 설계해줘"
- "여러 모듈에 걸친 기능 구현"

❌ 불필요:
- "User 엔티티 만들어줘"
- "회원가입 API 구현해줘"
```

### `/tdd` - TDD Guide Agent
```
✅ 사용:
- "TDD로 UserService 구현해줘"
- "테스트 먼저 작성하면서 개발하고 싶어"

❌ 불필요:
- 단순 설정 파일 수정
- 이미 테스트가 있는 코드 수정
```

### `/code-review` - Code Reviewer Agent
```
✅ 사용:
- 기능 구현 완료 후 PR 전
- "내 코드 검토해줘"
- 특정 모듈 전체 리뷰

❌ 불필요:
- 설정 파일만 변경했을 때
- 단순 오타 수정
```

## 워크플로우 예시

### 복잡한 기능 구현
```
1. /plan 동행 매칭 시스템
2. (계획 확인 후) "구현해줘" 또는 "/tdd 동행 매칭"
3. (구현 완료 후) /code-review
4. (리뷰 반영 후) 커밋 & PR
```

### 단순 기능 구현
```
1. "회원가입 API 만들어줘"
2. (구현 완료 후) 커밋
```

## 원칙

1. **필요할 때만 Agent 사용**
   - 단순 작업에 Agent 오버헤드 불필요
   
2. **순차적 사용**
   - plan → 구현 → code-review 순서
   
3. **직접 요청과 혼용 가능**
   - Agent 없이도 충분히 작업 가능
   - 복잡할 때만 Agent 활용
