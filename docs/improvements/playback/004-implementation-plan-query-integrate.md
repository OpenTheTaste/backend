# Implementation Plan: Query Integrate

## 0. 한 줄 요약

`query integrate` 단계에서는 success path의 DB round trip을 `SELECT + UPSERT`에서 `단일 SQL`로 줄이고, `affected rows == 0`은 fallback 검증으로 안전하게 해석한다. 이 문서는 메인 개선 문서가 아니라, 구현 직전 체크리스트로 둔다.

---

## 1. 목표

- success path DB 접근 `2회 -> 1회`
- `mediaId` API 계약 유지
- `affected rows == 0` 잘못된 `404` 방지
- 메인 개선이 아닌 핫패스 정리로 마무리

---

## 2. 비목표

- write-behind buffer
- Redis / in-memory buffer
- 클라이언트 주기 조정
- playback read API 구조 변경

---

## 3. 최종 동작 확정

### 3-1. 요청 계약

- request는 계속 `mediaId`, `positionSec`
- `contentsId`로 바꾸지 않음

### 3-2. success path

repository에서 한 번에 아래를 수행한다.

1. `mediaId -> contentsId`
2. `ACTIVE / PUBLIC / COMPLETED` 검증
3. playback upsert

### 3-3. row count 해석

- `affectedRows > 0` => 성공
- `affectedRows == 0` => fallback 검증

### 3-4. fallback 검증

fallback에서는 엔티티 조회가 아니라 `playable contents 존재 여부`만 확인한다.

- playable contents 존재 => `204 No Content` no-op 성공
- playable contents 없음 => `CONTENTS_NOT_FOUND`

---

## 4. 레이어별 책임

- Controller
  - 인증 memberId 주입
  - request validation
- Service
  - `positionSec` 보정
  - repository 결과 해석
  - `0건` fallback
  - 최종 예외 처리
- Repository
  - 단일 SQL upsert
  - row count 반환
- ContentsRepository
  - fallback용 `exists playable by mediaId`

---

## 5. 수정 예정 파일

- `apps/api-user/src/main/java/com/ott/api_user/playback/service/PlaybackService.java`
- `modules/domain/src/main/java/com/ott/domain/playback/repository/PlaybackRepository.java`
- `modules/domain/src/main/java/com/ott/domain/contents/repository/ContentsRepository.java`
- `apps/api-user/src/test/java/com/ott/api_user/playback/service/PlaybackServiceTest.java`
- 필요 시 repository integration test 파일

---

## 6. 구현 순서

| 순서 | 작업 | 목적 |
|------|------|------|
| 1 | `PlaybackRepository`에 `mediaId` 기반 단일 SQL upsert 추가 | success path DB 1회화 |
| 2 | `ContentsRepository`에 fallback exists query 추가 | `0건` 안전 해석 |
| 3 | `PlaybackService`를 `row count + fallback` 구조로 변경 | 서비스 책임 정리 |
| 4 | 기존 서비스 테스트 수정 | 현재 계약 유지 확인 |
| 5 | `0건 no-op`, `0건 not found` 테스트 추가 | edge case 고정 |
| 6 | 빌드/테스트 | 회귀 확인 |

---

## 7. 테스트 계획

서비스 테스트

1. 신규 insert 성공
2. 기존 row update 성공
3. `affectedRows == 0` + playable exists => 성공
4. `affectedRows == 0` + playable missing => `CONTENTS_NOT_FOUND`
5. 음수 `positionSec -> 0`

repository 검증

- MySQL row count 동작 확인
- 동일 `positionSec`, 같은 초, `modified_date = NOW()` 상황에서 `0건` 가능성 확인

---

## 8. 검증 명령

- `./gradlew :apps:api-user:test`
- `./gradlew clean build -x test`
- 필요 시 로컬 MySQL 환경에서 반복 호출 재현

---

## 9. 구현 완료 조건

- 수정 파일 확정
- 레이어 책임 정리
- fallback 동작 명확
- 테스트 시나리오 확정
- 검증 명령 확정
- 이 문서 기준으로 이제 구현 승인만 받으면 된다.
