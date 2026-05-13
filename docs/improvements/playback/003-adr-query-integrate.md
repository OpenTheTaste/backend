# ADR: Playback Query Integrate

## 0. 요약

이 문서는 Playback API의 메인 성능 개선 전 단계에서, 현재 `SELECT + UPSERT` 구조를 어떻게 정리할지에 대한 판단을 기록한다.

이번 결론은 아래와 같다.

- 요청 파라미터는 `contentsId`가 아니라 `mediaId`를 유지한다.
- 현재의 `SELECT -> 예외 처리 -> UPSERT` 구조는 이유가 있지만 hot path 기준으로 비싸다.
- 직접적인 메인 개선으로 보기는 어렵고, 본게임 전 `핫패스 정리` 성격의 세팅으로 본다.
- 적용 방향은 `단일 SQL + 0건 fallback 검증`이다.
- 단, `affected rows == 0`을 바로 `404`로 해석하면 안 된다.

---

## 1. Context

이 작업은 Playback의 최종 성능 개선 자체가 아니다.

정확히는 아래에 가깝다.

- 현재 write path의 불필요한 왕복 제거
- 서비스 레이어 책임 정리
- 이후 write-behind, 인메모리 버퍼, flush 설계로 가기 전 핫패스 정리

즉, 이 문서는 `본게임 전 세팅` 문서다.

---

## 2. 현재 Playback API 구조

현재 Playback API는 다음 순서로 동작한다.

1. 클라이언트가 `mediaId`, `positionSec`를 보낸다.
2. 서버는 `mediaId`로 `Contents`를 조회한다.
3. 이 조회에서 아래를 함께 검증한다.
- 콘텐츠 존재 여부
- `ACTIVE`
- `PUBLIC`
- `COMPLETED`
4. 검증 통과 시 `contentsId`를 얻는다.
5. `playback` 테이블에 native upsert를 수행한다.

현재 구조의 의미는 분명하다.

- `mediaId -> contentsId` 변환
- 비즈니스 조건 검증
- 잘못된 요청에 대한 예외 처리

즉, 현재의 SELECT는 단순히 "한 번 더 조회하는 낭비"는 아니다.

---

## 3. 그런데 왜 정리 대상인가

Playback API는 짧은 주기로 반복 호출되는 write API다.

이런 API에서 매 요청마다

- `SELECT 1회`
- `UPSERT 1회`

를 수행하는 구조는 비용이 크다.

특히 이 SELECT는 기능적으로는 맞지만, hot path 관점에서는 아래 문제가 있다.

- DB round trip이 2번이다.
- 서비스 레이어에서 엔티티 조회 책임이 남아 있다.
- 이후 더 큰 개선을 하더라도 현재 구조가 병목 설명을 복잡하게 만든다.

따라서 이 단계의 목적은 "SELECT를 완전히 없애는 것"이 아니라, `검증/변환/쓰기 경로를 더 짧고 안전하게 정리하는 것`이다.

---

## 4. request를 contentsId로 바꾸지 않는 이유

겉으로 보면 `mediaId` 대신 `contentsId`를 받으면 조회를 줄이기 쉬워 보인다.

하지만 이번 판단에서는 비추천한다.

이유는 아래와 같다.

- 외부 API 계약은 `mediaId` 중심으로 유지하는 편이 자연스럽다.
- `contentsId`는 현재 DB 구조에 더 가까운 내부 식별자다.
- API가 내부 테이블 구조에 묶인다.
- `contentsId`를 받더라도 `PUBLIC`, `ACTIVE`, `COMPLETED` 검증은 여전히 필요하다.

즉, `contentsId`로 바꿔 얻는 이득보다 API 결합도가 커지는 단점이 더 크다.

이번 정리의 기준은 `request는 mediaId 유지`다.

---

## 5. Decision: 단일 SQL + 0건 fallback 검증

이번에 선택하는 방향은 아래와 같다.

1. `INSERT ... SELECT ... ON DUPLICATE KEY UPDATE`로 검증, 변환, upsert를 한 번에 시도한다.
2. 성공 경로에서는 별도 SELECT를 수행하지 않는다.
3. 결과가 `0건`일 때만 fallback 검증을 수행한다.
4. fallback 결과에 따라 `정상 no-op` 또는 `CONTENTS_NOT_FOUND`를 구분한다.

핵심은 이것이다.

- 성공 경로는 최대한 가볍게
- 애매한 실패 경로만 한 번 더 확인

이 방향은 "항상 SELECT를 먼저 한다"와 "무조건 row 수만 보고 판단한다"의 중간 지점이다.

---

## 6. 왜 0건을 바로 404로 보면 안 되는가

이 포인트가 가장 중요하다.

MySQL의 `ON DUPLICATE KEY UPDATE`는 상황에 따라 affected rows 해석이 달라질 수 있다.

일반적으로 아래처럼 볼 수 있다.

- 새 INSERT: `1`
- 기존 row UPDATE, 값 변경됨: `2`
- 기존 row UPDATE, 값 동일함: `0` 또는 설정 영향
- SELECT 결과 자체 없음: `0`

즉 `0`은 한 가지 의미가 아니다.

현재 프로젝트는 이 리스크가 더 실제적이다.

- `playback.modified_date`는 `DATETIME`이다.
- 초 단위 정밀도다.
- 같은 초 안에 동일 `positionSec`이 다시 들어오면
  `position_sec`도 같고 `modified_date = NOW()`도 같을 수 있다.

이 경우 DB는 "실질 변경 없음"으로 보고 `affected rows = 0`을 반환할 수 있다.

그래서 아래와 같은 코드는 위험하다.

```java
if (affectedRows == 0) {
    throw new BusinessException(ErrorCode.CONTENTS_NOT_FOUND);
}
```

이렇게 하면 아래 두 상황이 구분되지 않는다.

- 진짜 콘텐츠 없음
- 대상은 맞지만 결과적으로 바뀐 값이 없음

따라서 `0건 = 바로 404`는 사용하지 않는다.

---

## 7. Before / After 흐름

### Before

```text
Client
  -> PUT /playback (mediaId, positionSec)
  -> Service
  -> SELECT contents by mediaId with ACTIVE/PUBLIC/COMPLETED check
  -> not found면 예외 처리
  -> UPSERT playback by contentsId
  -> 204 No Content
```

특징

- 성공 요청도 항상 DB 2회 접근
- service가 엔티티 조회와 예외 해석을 모두 담당

### After

```text
Client
  -> PUT /playback (mediaId, positionSec)
  -> Service
  -> 단일 SQL 실행
     - mediaId -> contentsId 변환
     - ACTIVE/PUBLIC/COMPLETED 검증
     - playback upsert
  -> affected rows > 0 이면 성공
  -> affected rows == 0 이면 fallback 검증
     - playable contents 존재하면 정상 no-op 성공
     - 없으면 CONTENTS_NOT_FOUND
  -> 204 No Content 또는 404
```

특징

- 성공 경로는 DB 1회 접근
- 애매한 경우에만 검증 SELECT 수행
- `0건` 해석 오류를 방지

---

## 8. Positive Consequences

### 8-1. 성공 경로 왕복 감소

현재의 `SELECT + UPSERT`를 성공 경로 기준 `1회`로 줄일 수 있다.

### 8-2. API 계약 유지

외부에는 계속 `mediaId`를 받으므로 클라이언트 계약을 흔들지 않는다.

### 8-3. 서비스 책임 정리

서비스 레이어에서 매번 엔티티를 조회하지 않아도 되므로 hot path가 더 단순해진다.

### 8-4. 0건 함정을 통제 가능

`row 수만 보고 실패 판단`하지 않고 fallback 검증을 넣음으로써 안전성을 확보한다.

### 8-5. 이후 메인 개선과도 충돌 없음

이 단계는 이후 write-behind, 인메모리 버퍼, flush 구조로 넘어갈 때도 그대로 도움이 된다.

---

## 9. Negative Consequences

### 9-1. SQL 복잡도 증가

repository 레벨 쿼리 가독성은 현재보다 떨어진다.

### 9-2. DB 문법 의존성 증가

MySQL 전용 upsert 문법과 row count 해석에 더 기대게 된다.

### 9-3. 실패 경로는 완전히 사라지지 않음

`0건` 케이스에서는 fallback 검증이 필요하므로, 실패 경로까지 완전히 1쿼리로 닫히지는 않는다.

### 9-4. 메인 개선은 아님

이 방식은 요청당 DB 접근 수를 줄이는 정리다.
하지만 Playback의 근본 문제인 "짧은 주기의 반복 write" 자체를 없애지는 못한다.

---

## 10. Expected Runtime Scenarios

### 10-1. 정상 콘텐츠, 위치 변경

- 단일 SQL이 정상적으로 실행된다.
- row 수는 보통 `1` 또는 `2`로 해석된다.
- fallback 없이 바로 성공한다.

### 10-2. 정상 콘텐츠, 같은 위치 재전송

- 같은 초 안에 동일 `positionSec`이면 `affected rows = 0` 가능성이 있다.
- 이 경우 fallback 검증이 동작한다.
- playable contents가 실제로 존재하면 `정상 no-op`로 처리한다.

### 10-3. 존재하지 않는 mediaId

- 단일 SQL 결과가 `0`일 수 있다.
- fallback 검증에서도 playable contents가 없으므로 `CONTENTS_NOT_FOUND`가 된다.

### 10-4. private 또는 not completed 콘텐츠

- 현재 계약상 외부에는 별도 사유를 노출하지 않는다.
- fallback 검증에서도 playable 조건을 만족하지 않으므로 동일하게 `CONTENTS_NOT_FOUND` 처리한다.

### 10-5. 짧은 시간에 같은 사용자가 여러 번 호출

- 여전히 같은 `(member_id, contents_id)` row에 대한 반복 update는 발생한다.
- 즉 row lock, commit 비용은 남아 있다.
- 다만 앞단 SELECT는 줄어든다.

### 10-6. 고트래픽 상황

- 현재보다 버티는 양은 늘 수 있다.
- 하지만 여전히 매 요청 DB write 구조이므로, 큰 부하에서는 결국 DB가 다시 병목이 된다.

---

## 11. Positioning

이 작업은 아래처럼 표현하는 것이 맞다.

- Playback API의 메인 성능 개선
- X

- Playback API의 write path 정리
- O

- 불필요한 DB 왕복 감소
- O

- 이후 메인 개선을 위한 사전 정비
- O

즉, 이 문서는 대표 개선보다는 `핫패스 구조 정리` 문서다. 필요하면 이후에는 overview 안의 한 섹션 수준으로 축약해도 된다.

---

## 12. Next Stage

이 정리를 한 뒤에도 Playback의 본게임은 남아 있다.

다음 단계에서 다룰 주제는 아래와 같다.

- 반복 write 자체를 줄일 것인가
- 인메모리 버퍼로 coalescing 할 것인가
- flush 주기와 최신값 조회를 어떻게 설계할 것인가
- 단일 서버와 다중 서버에서 어떤 전략이 다른가

즉, 이번 문서는 "DB 왕복 2번을 1번으로 줄이는 준비 단계"까지를 다룬다.

본게임은 그 다음이다.
