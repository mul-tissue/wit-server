---
name: tdd
description: TDD 방식으로 기능 구현
---

# /tdd [기능 설명]

TDD(Test-Driven Development) 방식으로 기능을 구현합니다.

## 사용법
```
/tdd 회원가입 기능 구현
/tdd UserService.create 메서드 구현
```

## 실행 내용

1. **테스트 케이스 목록 작성**
   - 성공 케이스
   - 실패 케이스 (예외 상황)
   - 엣지 케이스

2. **Red-Green-Refactor 사이클**
   - RED: 실패하는 테스트 작성
   - GREEN: 테스트 통과하는 최소 코드
   - REFACTOR: 코드 개선

3. **각 단계별 커밋**
   - 테스트 통과 시 커밋
   - 리팩토링 완료 시 커밋

## 주의사항
- 한 번에 하나의 테스트만 집중
- 테스트 없이 프로덕션 코드 작성 금지
- 커밋 메시지: `test(모듈): ...` 또는 `feat(모듈): ...`
