# Wit Backend API Plan

> 이 문서는 API 설계 및 구현 계획을 정리합니다.
> AI와 함께 작성하고, 이 문서를 기반으로 구현합니다.

## 작성 가이드

각 API는 아래 형식으로 작성:

```markdown
### API 이름
- **Endpoint**: `METHOD /api/v1/...`
- **설명**: 기능 설명
- **Request**: 요청 형식
- **Response**: 응답 형식
- **비즈니스 로직**: 핵심 로직 설명
- **에러 케이스**: 예외 상황
- **구현 상태**: [ ] 미완료 / [x] 완료
```

---

## Authentication 모듈

### 소셜 로그인 API
- **Endpoint**: `POST /v1/auth/login/social`
- **설명**: 카카오/구글/애플 소셜 로그인을 처리하고 JWT 토큰을 발급합니다.
- **Request**:
  ```json
  {
    "socialType": "KAKAO | GOOGLE | APPLE",
    "accessToken": "소셜 제공자 액세스 토큰"
  }
  ```
- **Response**:
  ```json
  {
    "publicId": "01JKUSER00000000000000001",
    "accessToken": "JWT 액세스 토큰",
    "refreshToken": "JWT 리프레시 토큰",
    "isNewUser": true
  }
  ```
- **비즈니스 로직**:
  1. OAuth 제공자에게 토큰 검증 요청 (RestClient 사용)
  2. 소셜 제공자 ID + 이메일 추출
  3. 기존 사용자 조회 또는 신규 생성 (PENDING 상태)
  4. JWT Access/Refresh Token 생성 및 Redis 저장
- **에러 케이스**:
  - `AUTH__001`: 유효하지 않은 OAuth 토큰
  - `AUTH__002`: 지원하지 않는 소셜 타입
- **구현 상태**: [x] 완료 (feat/4-social-login)

### 로그아웃 API
- **Endpoint**: `POST /v1/auth/logout`
- **설명**: Redis에서 Refresh Token을 삭제하여 로그아웃 처리합니다.
- **Request**: 없음 (헤더에서 userId 추출)
- **Response**: `204 No Content`
- **비즈니스 로직**:
  1. @CurrentUserId로 사용자 ID 추출
  2. Redis에서 Refresh Token 삭제
- **구현 상태**: [x] 완료 (feat/4-social-login)

---

## Terms 모듈

### 활성 약관 목록 조회 API
- **Endpoint**: `GET /v1/terms/active`
- **설명**: 현재 활성화된 약관 목록을 조회합니다. (인증 불필요)
- **Request**: 없음
- **Response**:
  ```json
  [
    {
      "publicId": "01JKTERMS001SERVICE00001",
      "type": "SERVICE",
      "title": "서비스 이용약관",
      "content": "약관 내용...",
      "version": "1.0",
      "required": true
    },
    {
      "type": "PRIVACY",
      "required": true
    },
    {
      "type": "MARKETING",
      "required": false
    }
  ]
  ```
- **비즈니스 로직**:
  - active = true인 약관만 조회
- **구현 상태**: [x] 완료 (feat/5-terms-agreement)

### 약관 동의 API
- **Endpoint**: `POST /v1/terms/agree`
- **설명**: 사용자가 약관에 동의하거나 철회합니다.
- **Request**:
  ```json
  {
    "agreements": [
      {
        "termsPublicId": "01JKTERMS001SERVICE00001",
        "agreed": true
      },
      {
        "termsPublicId": "01JKTERMS002PRIVACY00001",
        "agreed": true
      },
      {
        "termsPublicId": "01JKTERMS003MARKETNG0001",
        "agreed": false
      }
    ]
  }
  ```
- **Response**: `200 OK`
- **비즈니스 로직**:
  1. 모든 필수 약관(required=true)에 동의했는지 검증
  2. UserTermsAgreement 생성 또는 업데이트
  3. 선택 약관은 동의하지 않아도 OK
- **에러 케이스**:
  - `TERMS__001`: 약관을 찾을 수 없음
  - `TERMS__002`: 필수 약관 미동의
- **구현 상태**: [x] 완료 (feat/5-terms-agreement)

---

## User 모듈

### 온보딩 API
- **Endpoint**: `PATCH /v1/users/onboarding`
- **설명**: 신규 사용자의 닉네임, 성별, 출생연도를 설정하고 ACTIVE 상태로 전환합니다.
- **Request**:
  ```json
  {
    "nickname": "여행러버",
    "gender": "MALE | FEMALE | OTHER",
    "birthYear": 1990
  }
  ```
- **Response**: `200 OK`
- **비즈니스 로직**:
  1. @CurrentUserId로 사용자 조회
  2. 닉네임 2~20자 검증
  3. 출생연도 1900~2025 검증
  4. User 상태를 PENDING → ACTIVE로 변경
- **에러 케이스**:
  - `USER__001`: 사용자를 찾을 수 없음
  - `USER__002`: 이미 삭제된 사용자
- **구현 상태**: [x] 완료 (feat/6-onboarding)
- **참고**: 약관 동의는 이미 `/v1/terms/agree`에서 완료된 상태이므로 검증하지 않음

---

## Companion 모듈

> TODO: 동행 생성, 조회, 참여 등

---

## Feed 모듈

> TODO: 피드 CRUD 등

---

## Chat 모듈

> TODO: 채팅 관련

---

## Notification 모듈

> TODO: 알림 관련

---

## 변경 이력

| 날짜 | 변경 내용 |
|------|----------|
| 2025-02-05 | 문서 생성 |
| 2025-02-06 | Authentication, Terms, User 모듈 API 계획 작성 및 구현 완료 |
