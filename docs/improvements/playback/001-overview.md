# Playback Improvement Overview

## 0. 한 줄 요약

Playback 개선은 `2단계`로 나뉜다. 먼저 `query integrate`로 현재 `SELECT + UPSERT` hot path를 가볍게 정리하고, 그 다음 `write-behind` 기반 본게임 개선으로 `매 요청 DB write` 자체를 줄인다.

---

## 1. 이 패키지가 다루는 범위

대상 API는 `PUT /playback`이다.

이 API는 다음 특성을 가진다.

- 호출 빈도가 높다.
- 같은 `(memberId, contentId)` 키에 대한 반복 write가 많다.
- read query 튜닝보다 `write amplification` 감소가 더 중요하다.

그래서 이번 패키지는 개선을 아래 두 층위로 나눠서 관리한다.

| 단계 | 역할 | 비중 |
|------|------|------|
| `query integrate` | 성공 경로의 DB 왕복을 줄이고 hot path를 정리 | 보조 단계 |
| `write improvement` | 반복 write 자체를 병합하거나 버퍼링해서 DB 부하를 줄임 | 메인 개선 |

---

## 2. 현재 확정된 내용

`query integrate` 단계에서 이미 확정한 내용은 아래와 같다.

- request는 `contentsId`가 아니라 `mediaId`를 유지한다.
- 현재 `SELECT -> 예외 처리 -> UPSERT` 구조는 정리 대상이다.
- 적용 방향은 `단일 SQL + 0건 fallback 검증`이다.
- `affected rows == 0`은 바로 `404`로 해석하지 않는다.
- 이 단계는 메인 개선이 아니라 구현 전에 정리해두는 보조 단계다.

이 단계는 메인 성능 개선이 아니라 `핫패스 정리`다.

---

## 3. 현재까지 확인된 병목

Before 부하 테스트 기준 관찰값은 아래와 같다.

- `vu500`: avg `34ms`, WPS `95/s`
- `vu1000`: avg `1596ms`, WPS `145/s`
- `vu5000`: avg `23542ms`, WPS `160/s`
- `vu10000`: avg `45391ms`, error rate `13.9%`

즉, 현재 구조는 대략 `170/s` 부근에서 상한이 보이고, 그 이후에는 응답 시간과 에러율이 빠르게 무너진다.

---

## 4. 문서 구성

이 패키지는 아래 순서로 읽으면 된다.

1. [002-as-is.md](</C:\Users\kkwas\OneDrive\desktop\유레카\최종 융합 프로젝트\backend\docs\improvements\playback\002-as-is.md>)
   현재 API 구조, 병목, before 수치
2. [003-adr-query-integrate.md](</C:\Users\kkwas\OneDrive\desktop\유레카\최종 융합 프로젝트\backend\docs\improvements\playback\003-adr-query-integrate.md>)
   왜 `mediaId 유지 + 단일 SQL + 0건 fallback`을 선택했는지
3. [004-implementation-plan-query-integrate.md](</C:\Users\kkwas\OneDrive\desktop\유레카\최종 융합 프로젝트\backend\docs\improvements\playback\004-implementation-plan-query-integrate.md>)
   query 작업의 구현 직전 체크리스트
4. [005-write-improvement-plan.md](</C:\Users\kkwas\OneDrive\desktop\유레카\최종 융합 프로젝트\backend\docs\improvements\playback\005-write-improvement-plan.md>)
   본게임 개선안과 대안 비교

---

## 5. 지금 상태에서 바로 설명 가능한 포인트

- 현재 구조는 기능적으로는 안전하지만 hot path 기준 비싸다.
- query 단계는 `메인 개선`이 아니라 `구현 전 세팅`이다.
- 본게임은 결국 `매 요청 DB write`를 어떻게 줄일 것인가에 있다.
- 그래서 설명 순서는 `As-Is -> Query 정리 -> Write 개선`으로 가져가는 것이 자연스럽다.
- 문서 비중도 `005-write-improvement-plan.md`에 두고, `003`, `004`는 구현 보조 문서로 본다.

---

## 6. 다음 액션

현재 playback 패키지에서 구현 직전까지 닫힌 작업은 `query integrate`다.

이후 순서는 아래가 된다.

1. `query integrate` 실제 코드 반영
2. 결과 검증
3. `write improvement` 설계 확정
4. 본게임 구현 및 after 측정
