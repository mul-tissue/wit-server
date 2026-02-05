---
name: code-review
description: 코드 품질 검토
---

# /code-review [파일 또는 디렉토리]

작성한 코드의 품질, 보안, 아키텍처 준수 여부를 검토합니다.

## 사용법
```
/code-review                           # 최근 변경된 파일 리뷰
/code-review src/main/java/com/wit/user  # 특정 모듈 리뷰
/code-review UserService.java          # 특정 파일 리뷰
```

## 검토 항목

### 필수 체크
- [ ] 모듈 경계 규칙 준수 (다른 모듈 Repository 주입 금지)
- [ ] Entity에 setter 없음
- [ ] 적절한 ErrorCode 사용
- [ ] 보안 이슈 없음

### 권장 체크
- [ ] 메서드 단일 책임
- [ ] 적절한 테스트 존재
- [ ] 명확한 네이밍

## 출력
- Critical / Warning / Info 레벨로 이슈 분류
- 구체적인 개선 방안 제시
- 잘한 점도 언급
