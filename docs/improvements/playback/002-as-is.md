# Playback As-Is

## 0. 한 줄 요약

현재 `PUT /playback`은 `mediaId` 기반 `Contents` 조회 후 `playback` upsert를 수행한다. 기능적으로는 안전하지만, 고빈도 반복 write 경로에서 성공 요청도 DB를 2번 치는 구조라 병목을 키운다.

---

## 1. 엔드포인트

- Method / Path: `PUT /playback`
- 목적: 사용자의 현재 재생 위치(`positionSec`) 저장/갱신
- 요청 파라미터: `mediaId`, `positionSec`

---

## 2. 현재 요청 흐름

```text
Client
  -> PUT /playback
  -> PlaybackService
  -> Contents 조회
     - mediaId -> contentsId 변환
     - ACTIVE / PUBLIC / COMPLETED 검증
  -> playback upsert
  -> 204 No Content
```

핵심은 service가 `mediaId -> contentsId` 변환, 재생 가능 검증, 예외 해석, upsert 호출을 모두 담당한다는 점이다.

---

## 3. 현재 SELECT의 역할

1. `mediaId -> contentsId` 변환
2. 콘텐츠 존재 여부 검증
3. `ACTIVE / PUBLIC / COMPLETED` 조건 검증
4. 실패 시 `CONTENTS_NOT_FOUND` 예외 처리

즉, 현재 SELECT는 단순 낭비가 아니라 `변환 + 검증 + 예외 해석` 역할을 같이 가지고 있다.

---

## 4. 그런데 왜 정리 대상인가

Playback은 짧은 주기로 반복 호출되는 write API다.

현재 구조는 성공 경로에서도 매 요청마다 아래를 수행한다.

- `SELECT 1회`
- `UPSERT 1회`

문제는 아래와 같다.

- DB round trip이 2번이다.
- service가 엔티티 조회 책임을 가진다.
- 반복 write에서 connection pool과 DB commit 비용을 빠르게 소모한다.

이 단계의 목적은 `SELECT 제거` 자체가 아니라 `성공 경로 DB 왕복 감소`다.

---

## 5. Before 측정 결과

측정 환경

- 고정 `mediaId`
- 5초 주기 갱신
- ramp-up `10s` + sustain `3m` + ramp-down `5s`
- MySQL 8 Docker
- HikariCP `maximumPoolSize=10`

| VU | avg (ms) | p90 (ms) | max (ms) | WPS | Error Rate |
|----|----------|----------|----------|-----|------------|
| 500 | 34 | 68 | 267 | 95/s | 0% |
| 1000 | 1596 | 1980 | 3602 | 145/s | 0% |
| 5000 | 23542 | 28425 | 31352 | 160/s | 0% |
| 10000 | 45391 | 59999 | 60003 | 174/s | 13.9% |

---

## 6. 현재까지 읽힌 병목

1. WPS 상한이 대략 `170/s` 부근에서 정체된다.
2. `vu500 -> vu1000` 구간에서 응답 시간이 급격히 튄다.
3. 반복 write가 connection pool과 DB commit 비용을 압박한다.
4. 현재 구조는 hot path에서 service 역할이 과하다.

---

## 7. 이 문서의 역할

이 문서는 현재 구조와 병목을 고정하는 문서다.

이 단계는 query 정리의 배경 설명까지를 담당하고, playback 개선의 중심 설명은 이후 write improvement 문서에 둔다.

다음 문서에서 아래를 다룬다.

- 왜 request는 `mediaId`를 유지하는가
- 왜 `단일 SQL + 0건 fallback 검증`으로 가는가
- 왜 `0 row != 404`인가
