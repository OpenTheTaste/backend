# CodeRabbit Review Guidelines (Backend)

이 문서는 PR 코드리뷰에서 CodeRabbit이 참고할 프로젝트 코딩 규칙이다.
CodeRabbit 사이트의 **Repository Settings > Review Instructions** 에 아래 내용을 붙여넣는다.

---

## 1) 리뷰 우선순위

- **P0 (머지 차단)**: 컴파일 실패, 부팅 실패, 인가 누락, 데이터 무결성 훼손, SQL injection/XSS 등 보안 취약점
- **P1 (머지 전 보완 권장)**: API 계약 불일치, 페이징/조회 품질, 상태 전이 누락, N+1 쿼리
- **P2 (품질 개선)**: 중복 제거, 테스트 가독성, 불필요한 import

---

## 2) 프로젝트 구조 및 모듈 경계

### 멀티모듈 구성
```
backend/
├── apps/
│   ├── api-user/       # 사용자 API (포트 8080, Flyway 실행 주체)
│   ├── api-admin/      # 관리자 API (포트 8081)
│   └── transcoder/     # 트랜스코더 (포트 8082)
├── modules/
│   ├── domain/         # JPA 엔티티 + Repository + enum
│   ├── infra/          # Flyway 마이그레이션, S3, 외부 연동
│   ├── common-web/     # 응답(SuccessResponse/PageResponse), 예외, Swagger 설정
│   └── common-security/# JWT, Security 필터
```

### 의존 규칙
- `apps/*` → `common-web`, `common-security`, `infra`, `domain` 의존 가능.
- `common-web`에 DB(JPA) 의존성 추가 금지.
- `domain` 모듈은 웹 모듈에 의존하지 않는다.
- 앱 내부에 공통 DTO/유틸 중복 생성 금지 — `common-web`에 이미 있는 것을 사용한다.

### 앱 내부 패키지 구조
```
com.ott.{app-name}/{도메인}/
├── controller/      # @RestController (implements XxxApi)
├── service/         # 비즈니스 로직
├── mapper/          # DTO 변환 (@Component)
└── dto/
    ├── request/
    └── response/
```

### 도메인 모듈 패키지 구조
```
com.ott.domain.{도메인명}/
├── domain/          # @Entity
└── repository/      # JpaRepository + Custom + Impl
```

---

## 3) Controller 규칙

### API 인터페이스 분리 패턴
모든 컨트롤러는 **API 인터페이스 + 구현 컨트롤러**로 분리한다.
- `XxxApi.java` (interface): Swagger 어노테이션(`@Tag`, `@Operation`, `@ApiResponses`, `@Parameter`) 전담.
- `XxxController.java` (class): `implements XxxApi`, Spring MVC 어노테이션(`@GetMapping` 등)과 서비스 호출만 담당.

```java
// API 인터페이스
@Tag(name = "BackOffice Series API", description = "[백오피스] 시리즈 관리 API")
public interface BackOfficeSeriesApi {
    @Operation(summary = "시리즈 목록 조회")
    @ApiResponses(...)
    ResponseEntity<SuccessResponse<PageResponse<SeriesListResponse>>> getSeries(...);
}

// 컨트롤러 구현
@RestController
@RequestMapping("/back-office")
@RequiredArgsConstructor
public class BackOfficeSeriesController implements BackOfficeSeriesApi {
    @Override
    @GetMapping("/admin/series")
    public ResponseEntity<SuccessResponse<PageResponse<SeriesListResponse>>> getSeries(...) {
        return ResponseEntity.ok(SuccessResponse.of(service.getSeries(...)));
    }
}
```

### Controller 주의사항
- Controller에서 직접 `BusinessException` throw 금지 — 검증 어노테이션(`@Valid`, `@NotBlank` 등) 사용.
- 엔티티를 Response로 직접 반환 금지 — 반드시 DTO 변환.
- `@RequestParam`에 `value` 명시: `@RequestParam(value = "page", defaultValue = "0")`.
- `@PathVariable`에 이름 명시: `@PathVariable("memberId")`.
- 페이지/사이즈 파라미터는 `Integer` (boxed type) 사용.

### 응답 래핑 패턴
| 상황 | 패턴 |
|------|------|
| 조회 (단건/일반) | `ResponseEntity.ok(SuccessResponse.of(data))` |
| 조회 (페이징) | `ResponseEntity.ok(SuccessResponse.of(PageResponse))` |
| 변경 (반환값 있음) | `SuccessResponse.of(data).asHttp(HttpStatus.OK)` |
| 변경 (반환값 없음) | `ResponseEntity.noContent().build()` (204) |

---

## 4) Service 규칙

### 트랜잭션 어노테이션
- 조회 메서드: `@Transactional(readOnly = true)`
- 변경 메서드: `@Transactional`
- 메서드 단위로 명시하는 것을 기본으로 한다.

### 예외 처리
- Service에서 `BusinessException(ErrorCode.XXX)` throw.
- 시스템 예외를 `BusinessException`으로 래핑하지 않는다 — `GlobalExceptionHandler`가 처리.
- `orElseThrow(() -> new BusinessException(ErrorCode.XXX))` 패턴 사용.

### 페이징 패턴 (3단계)
```java
// 1. Pageable 생성
Pageable pageable = PageRequest.of(page, size);

// 2. 조회
Page<Media> mediaPage = repository.findXxx(pageable, ...);

// 3. DTO 변환 + PageResponse 생성
List<XxxResponse> responseList = mediaPage.getContent().stream()
        .map(entity -> mapper.toXxxResponse(entity, ...))
        .toList();
PageInfo pageInfo = PageInfo.toPageInfo(mediaPage.getNumber(), mediaPage.getTotalPages(), mediaPage.getSize());
return PageResponse.toPageResponse(pageInfo, responseList);
```

### Service ↔ Mapper 역할 분리
- **Service**: 데이터 조회, 추출 (예: `media.getUploader().getNickname()`), 비즈니스 로직 처리.
- **Mapper**: 순수 DTO 변환만 담당. Service에서 추출한 값을 파라미터로 받는다.

---

## 5) DTO 규칙

### Response DTO
- **`record`** 사용.
- 클래스 레벨 `@Schema(description = "...")` 필수.
- 모든 필드에 `@Schema(type = "...", description = "...", example = "...")` 필수.
- `type`은 문자열로 명시: `"Long"`, `"String"`, `"List<String>"` 등.

```java
@Schema(description = "시리즈 목록 조회 응답")
public record SeriesListResponse(
        @Schema(type = "Long", description = "미디어 ID", example = "1")
        Long mediaId,
        @Schema(type = "List<String>", description = "태그 이름 목록", example = "[\"스릴러\"]")
        List<String> tagNameList
) {}
```

### Request DTO
- 단순 요청: `record` + 필드에 검증 어노테이션.
- 복잡한 요청 (다수 필드, 중첩 검증): `class` + `@Getter @NoArgsConstructor` + 필드에 검증 어노테이션.
- `@Valid @RequestBody`로 바인딩.

### 공통
- 내부 전달용 DTO: `class` + `@Getter @AllArgsConstructor` (Swagger 어노테이션 불필요).

---

## 6) Entity 규칙

### 어노테이션 순서 (고정)
```java
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Builder
@Getter
@Table(name = "snake_case_table_name")
public class MyEntity extends BaseEntity { ... }
```

### 필수 규칙
- 모든 엔티티는 `BaseEntity` 상속 (제공: `createdDate`, `modifiedDate`, `status`).
- `@Setter` 사용 금지 — 변경은 명시적 비즈니스 메서드로만 (`updateXxx()`, `changeXxx()`, `clearXxx()`).
- 생성은 `static` 팩토리 메서드 또는 `@Builder`.
- 연관관계는 반드시 `FetchType.LAZY`.
- Enum 매핑은 반드시 `@Enumerated(EnumType.STRING)` (ORDINAL 금지).
- `@Column(name = "snake_case")` 명시.
- URL 필드: `columnDefinition = "TEXT"`.

### 도메인 로직 위치
- 상태 전이 검증, 불변조건 등은 엔티티 내부 메서드에서 처리.
```java
public void changeRole(Role targetRole) {
    if (!this.role.canTransitionTo(targetRole))
        throw new IllegalArgumentException("Invalid role transition");
    this.role = targetRole;
}
```

---

## 7) Repository 규칙

### 3파일 구조 (Custom Repository)
동적 쿼리가 필요한 도메인은 반드시 아래 구조:
```
XxxRepository.java        → extends JpaRepository<E, Long>, XxxRepositoryCustom
XxxRepositoryCustom.java  → interface (커스텀 메서드 시그니처)
XxxRepositoryImpl.java    → implements XxxRepositoryCustom (QueryDSL 구현)
```

### QueryDSL 사용 원칙
- **JPQL 사용 금지** — 모든 커스텀 쿼리는 QueryDSL로 작성.
- Q-class는 `static import`로 사용: `import static com.ott.domain.xxx.domain.QXxx.xxx;`
- `@RequiredArgsConstructor` + `JPAQueryFactory` 주입.

### 페이징 쿼리 패턴
```java
List<Entity> resultList = queryFactory
        .selectFrom(entity)
        .where(조건들...)
        .orderBy(entity.createdDate.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

JPAQuery<Long> countQuery = queryFactory
        .select(entity.count())
        .from(entity)
        .where(조건들...);

return PageableExecutionUtils.getPage(resultList, pageable, countQuery::fetchOne);
```

### 동적 조건 헬퍼 (null-safe BooleanExpression)
필터가 없을 때 `null` 반환 — QueryDSL이 `null` 조건을 무시한다.
```java
private BooleanExpression titleContains(String searchWord) {
    if (StringUtils.hasText(searchWord))
        return media.title.contains(searchWord);
    return null;
}
```

### Fetch Join
- N+1 방지를 위해 연관 엔티티 조회 시 `fetchJoin()` 사용.
- 단건 조회: `.fetchOne()` + `Optional.ofNullable()` 래핑.
- 목록(IN절): 별도 메서드로 분리, `.in()` 사용.
- 단건 vs 목록은 별도 메서드 — 단건에 `List.of(id)` + IN절 사용 금지.

### 메서드 네이밍
- `findWith{조인대상}By{조건}`: `findWithMediaAndUploaderByMediaId`
- `findWith{조인대상}By{조건}s` (복수): `findWithTagAndCategoryByMediaIds`

---

## 8) Mapper 규칙

### 구현 방식
- MapStruct 미사용. 순수 `@Component` 클래스로 수동 매핑.
- 메서드명: `to{TargetDtoType}` (예: `toSeriesListResponse`, `toMemberListResponse`).
- 반복되는 추출 로직은 `private` 헬퍼 메서드로 분리 (예: `extractCategoryName`, `extractTagNameList`).
- 스트림에서 메서드 참조로 사용 가능: `mapper::toXxxResponse`.

---

## 9) Enum 규칙

- 모든 enum은 `@AllArgsConstructor @Getter` + `key`/`value` 두 필드 구조.
```java
@AllArgsConstructor
@Getter
public enum MediaType {
    SERIES("SERIES", "시리즈"),
    CONTENTS("CONTENTS", "콘텐츠"),
    SHORT_FORM("SHORT_FORM", "숏폼");
    String key;
    String value;
}
```
- 상태 등의 전이는 enum 내부 메서드도 적극 활용.

---

## 10) 네이밍 규칙

### 컬렉션 변수
- 반드시 `도메인의미 + List` 접미사: `mediaIdList`, `tagNameList`, `responseList`, `mediaTagList`.
- 단순 복수형(`tags`, `users`, `items`) 금지.
- DTO 필드, 서비스 로컬 변수, 응답 필드 모두 동일 기준.

### 클래스 네이밍
| 대상 | 패턴 | 예시 |
|------|------|------|
| 컨트롤러 (사용자) | `{Domain}Controller` | `SearchController` |
| 컨트롤러 (관리자) | `BackOffice{Domain}Controller` | `BackOfficeSeriesController` |
| API 인터페이스 | `{Domain}Api` / `BackOffice{Domain}Api` | `BackOfficeSeriesApi` |
| 서비스 | `{Domain}Service` / `BackOffice{Domain}Service` | `BackOfficeSeriesService` |
| 매퍼 | `BackOffice{Domain}Mapper` | `BackOfficeSeriesMapper` |
| Request DTO | `{동작}Request` | `AdminLoginRequest`, `ChangeRoleRequest` |
| Response DTO | `{Domain}{List/Detail}Response` | `SeriesListResponse`, `SeriesDetailResponse` |

### 메서드 네이밍
| 레이어 | 패턴                                               | 예시 |
|--------|--------------------------------------------------|------|
| Service (조회) | `get{Domain}List`, `get{Domain}Detail`           | `getSeriesList`, `getSeriesDetail` |
| Service (변경) | 동사 + 명사                                          | `changeRole` |
| Mapper | `to{TargetDto}`                                  | `toSeriesListResponse` |
| Entity (변경) | `update{Field}`, `change{Field}`, `clear{Field}` | `updateRefreshToken` |
| QueryDSL 헬퍼 | 필드명 + 조건                                         | `titleContains`, `roleEq` |

[//]: # (| Repository | `findWith{Joins}By{Condition}`                   | `findWithMediaAndUploaderByMediaId` |) 임시 제거
---

## 11) Lombok 사용 규칙

| 어노테이션 | 사용 위치 | 비고 |
|-----------|----------|------|
| `@Getter` | 엔티티, class 기반 DTO | record에는 불필요 |
| `@NoArgsConstructor(PROTECTED)` | 모든 엔티티 | JPA 프록시 용 |
| `@AllArgsConstructor(PRIVATE)` | 모든 엔티티 | Builder 강제 |
| `@Builder` | 엔티티 | 생성 전용 |
| `@RequiredArgsConstructor` | Controller, Service, Mapper, Impl | 생성자 주입 |
| `@Slf4j` | ExceptionHandler, Security 핸들러 | |
| `@Setter` | **사용 금지** | 어디에서든 금지 |

- `@Autowired` 필드 주입 금지 — `@RequiredArgsConstructor` + `private final` 사용.

---

## 12) 권한/보안

- Spring Security: `STATELESS` 세션 + JWT 필터.
- ADMIN 전용 백오피스 API에 `permitAll` 금지.
- `@Bean SecurityFilterChain`에서 URL별 인가 설정.
- `permitAll` 허용 대상: `/actuator/**`, `/swagger-ui/**`, `/v3/api-docs/**` 등 인프라 엔드포인트만.
- EDITOR 제한: 롱폼 업로드, 시리즈 관리, 사용자 관리, 대시보드 접근 불가.

---

## 13) DB/Flyway 규칙

- 마이그레이션 경로: `modules/infra/src/main/resources/db/migration`
- 파일명: `V{version}__{description}.sql`
- 운영 모드: `ddl-auto: validate` (Hibernate가 스키마 자동 변경하지 않음)
- Flyway 실행 주체: `api-user` 앱만.
- 스키마 변경 PR은 반드시 SQL + 엔티티 + 영향 범위를 함께 리뷰.

---

## 14) Media 테이블 계층 (V2 구조)

- `media` 테이블이 공통 부모 (Class Table Inheritance, 비식별 1:1).
- `series`, `contents`, `short_form`은 각자 자체 PK + `media_id` UNIQUE FK.
- 공통 필드(`title`, `description`, `posterUrl`, `thumbnailUrl`, `publicStatus`, `bookmarkCount`, `likesCount`, `uploader`)는 반드시 `Media` 경유 접근.
  - `series.getMedia().getTitle()` (O)
  - `series.getTitle()` (X — 필드 없음)
- 태그: `media_tag` 테이블 중심. 구 `series_tag`/`contents_tag` 사용 금지.
- `bookmark`/`likes`: `media_id` FK로 통합. 구 `target_id + target_type` 패턴 사용 금지.
- `ingest_job`: `media_id` FK 사용. 구 `contents_id + short_form_id` 패턴 사용 금지.

---

## 15) ErrorCode 체계

| 접두사 | 카테고리 | 예시 |
|--------|----------|------|
| C0XX | Common | `INVALID_INPUT`, `MISSING_PARAMETER` |
| A0XX | Auth | `UNAUTHORIZED`, `INVALID_TOKEN`, `EXPIRED_TOKEN` |
| U0XX | User | `USER_NOT_FOUND`, `DUPLICATE_EMAIL` |
| B0XX | Business | `CONTENT_NOT_FOUND`, `SERIES_NOT_FOUND` |

- 새 에러 코드: `modules/common-web/.../exception/ErrorCode.java`에 추가.

---

## 16) 리뷰 코멘트 형식

CodeRabbit이 코멘트를 남길 때 아래 3가지를 포함:
- **심각도**: P0 / P1 / P2
- **근거**: 파일명:라인번호
- **수정 제안**: 구체적인 코드 또는 구조 변경안

---

## 17) 최소 검증 기준

- `./gradlew clean build -x test` — 컴파일 성공
- `./gradlew test` — 테스트 통과
- DB 변경 포함 PR: Flyway 적용 후 부팅 검증

---

## 부록: 팀 내 확정 사항

아래 항목은 현재 코드베이스에서 일관되지 않거나, 명시적으로 확정되지 않은 부분이다. 팀 내 논의 후 결정하면 위 규칙에 반영한다.

### A. Response 래핑 스타일 통일
현재 두 가지 스타일이 혼용됨:
- `ResponseEntity.ok(SuccessResponse.of(service.method()))` — SeriesController, MemberController 선택

### B. @Transactional 선언 위치
현재 두 가지 스타일 혼용:
- 메서드 단위 명시 (BackOfficeSeriesService, BackOfficeMemberService) 선택

### C. Request DTO — record vs class 기준
- `record` (ChangeRoleRequest) 선택

### D. Mapper에서의 데이터 추출 범위
Mapper 내에서 `extractCategoryName`, `extractTagNameList` 같은 가공 로직이 존재한다.
Service가 원시 데이터를 넘기고 Mapper가 가공하는 것.

### E. 테스트 전략
- 아직 미정.