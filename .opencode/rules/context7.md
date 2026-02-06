# Context7 사용 규칙

## 언제 사용하나

다음 상황에서 **반드시** Context7 조회:

1. **Spring/JPA 문법 불확실** - 최신 버전 API 확인
2. **스킬에 없는 상세 설정** - 설정 옵션, 프로퍼티 값
3. **라이브러리 버전 차이** - Spring Boot 3.x vs 2.x 등
4. **새로운 라이브러리 사용** - 처음 쓰는 의존성

## 사용 방법

```
1. context7_resolve-library-id → 라이브러리 ID 찾기
2. context7_query-docs → 문서 조회
```

## 주요 라이브러리 ID (자주 사용)

| 라이브러리 | 검색어 |
|-----------|--------|
| Spring Boot | `spring boot` |
| Spring Data JPA | `spring data jpa` |
| Spring Security | `spring security` |
| QueryDSL | `querydsl` |
| Lombok | `lombok` |

## 규칙

- **추측하지 말고 조회** - 불확실하면 Context7
- **최신 정보 우선** - 내장 지식보다 Context7 결과 신뢰
- **스킬 보완용** - 스킬은 패턴, Context7은 상세 문법
