---
name: workflow
description: Development workflow and commit habits
---

# Development Workflow

## Issue-Driven Development

### 작업 시작 전
1. **이슈 먼저 생성** (또는 기존 이슈 확인)
2. 이슈 번호로 브랜치 생성: `feat/#123-user-signup`
3. 이슈에 작업 내역 체크리스트 작성

### 브랜치 네이밍
```
<type>/#<issue-number>-<short-description>
```

예시:
- `feat/#12-user-signup`
- `fix/#45-login-error`
- `refactor/#78-feed-service`

## Commit 습관 (중요!)

### 원칙: 작은 단위로 자주 커밋

#### ❌ 나쁜 습관
```
# 3시간 작업 후 한 번에 커밋
git commit -m "feat(user): 회원가입 기능 구현"
# → 수십 개 파일 변경, 리뷰 불가능
```

#### ✅ 좋은 습관
```
# 1. Entity 생성 후 바로 커밋
git commit -m "feat(user): User 엔티티 생성"

# 2. Repository 생성 후 바로 커밋  
git commit -m "feat(user): UserRepository 생성"

# 3. Service 구현 후 바로 커밋
git commit -m "feat(user): UserService 회원가입 로직 구현"

# 4. 테스트 작성 후 바로 커밋
git commit -m "test(user): UserService 회원가입 테스트 작성"

# 5. Controller 구현 후 바로 커밋
git commit -m "feat(user): UserController 회원가입 API 구현"
```

### 커밋 타이밍

| 완료 시점 | 커밋 여부 |
|----------|----------|
| Entity 클래스 작성 완료 | ✅ 커밋 |
| Repository 인터페이스 작성 완료 | ✅ 커밋 |
| Service 메서드 하나 구현 완료 | ✅ 커밋 |
| 테스트 케이스 작성 완료 | ✅ 커밋 |
| Controller 엔드포인트 하나 구현 완료 | ✅ 커밋 |
| 버그 수정 완료 | ✅ 커밋 |
| 리팩토링 완료 | ✅ 커밋 |
| 설정 파일 변경 | ✅ 커밋 |

### AI 에이전트 커밋 규칙

AI가 코드를 작성할 때:
1. **기능 단위로 커밋 요청**: "Entity 만들었으면 커밋해줘"
2. **테스트 통과 후 커밋**: 테스트가 있다면 통과 확인 후
3. **논리적 단위 유지**: 한 커밋 = 한 가지 변경 목적

```
# AI에게 요청 예시
"UserService 구현하고 커밋해줘. 그 다음 테스트 작성하고 또 커밋해줘."
```

## TDD 워크플로우

```
1. 실패하는 테스트 작성 → 커밋 (선택)
2. 테스트 통과하는 코드 작성 → 커밋
3. 리팩토링 → 커밋
```

### 테스트와 구현 분리 커밋

```bash
# 구현 먼저
git commit -m "feat(user): UserService 회원가입 구현"

# 테스트 별도
git commit -m "test(user): UserService 회원가입 테스트"
```

또는

```bash
# 테스트와 구현 함께 (작은 기능일 때)
git commit -m "feat(user): UserService 회원가입 구현 및 테스트"
```

## PR 워크플로우

### PR 생성 시점
- 이슈의 모든 작업 완료 시
- 또는 중간 리뷰가 필요할 때 (Draft PR)

### PR 체크리스트
- [ ] 모든 테스트 통과
- [ ] Spotless 포맷팅 적용
- [ ] 이슈 번호 연결
- [ ] 작업 내용 요약

### PR 크기
- 이상적: 200-400 줄 변경
- 최대: 500 줄 이하
- 너무 크면 이슈 분리 고려

## 커밋 메시지 Quick Reference

```bash
# 기능
feat(user): User 엔티티 생성
feat(user): UserRepository 생성
feat(user): UserService 회원가입 구현
feat(user): UserController 회원가입 API

# 테스트
test(user): UserService 단위 테스트
test(user): UserController 통합 테스트

# 수정
fix(user): 회원가입 시 중복 검사 오류 수정

# 리팩토링
refactor(user): UserService 메서드 분리

# 설정
chore: Swagger 설정 추가
chore: application.yml 프로필 분리
```
