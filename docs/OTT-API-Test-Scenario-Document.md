# OTT API Test Scenario & Document

**Version 1.0**  
**2026.03**

---

## Table of Contents

| Table of Contents |
|-------------------|
| [1. api-user 인증 (Auth)](#1-api-user-인증-auth) |
| [2. api-user 회원 (Member)](#2-api-user-회원-member) |
| [3. api-user 콘텐츠·시리즈 조회](#3-api-user-콘텐츠시리즈-조회) |
| [4. api-user 숏폼 피드 및 이벤트](#4-api-user-숏폼-피드-및-이벤트) |
| [5. api-user 재생목록 (Playlist)](#5-api-user-재생목록-playlist) |
| [6. api-user 재생·시청기록·북마크·좋아요](#6-api-user-재생시청기록북마크좋아요) |
| [7. api-user 댓글 (Comment)](#7-api-user-댓글-comment) |
| [8. api-user 태그·카테고리](#8-api-user-태그카테고리) |
| [9. api-admin 인증 (Back Office Auth)](#9-api-admin-인증-back-office-auth) |
| [10. api-admin 업로드 (Presigned URL)](#10-api-admin-업로드-presigned-url) |
| [11. api-admin 조회 및 관리](#11-api-admin-조회-및-관리) |
| [12. 공통 예외 처리 및 응답 포맷](#12-공통-예외-처리-및-응답-포맷) |
| [13. API Load Test](#13-api-load-test) |

---

## 1. api-user 인증 (Auth)

OAuth2 카카오 로그인, 토큰 재발급, 로그아웃 테스트

### 1.1 토큰 재발급

| TC-ID | 시나리오 | 사전조건 | 입력 | 기대결과 | 판정기준 |
|-------|----------|----------|------|----------|----------|
| UA-001 | 유효한 refreshToken으로 재발급 | 카카오 로그인 완료, 쿠키에 refreshToken 존재 | POST /auth/reissue (쿠키) | 204 No Content, accessToken/refreshToken 쿠키 갱신 | 쿠키 확인, 이후 인증 필요 API 호출 성공 |
| UA-002 | refreshToken 없이 재발급 | 쿠키 없음 | POST /auth/reissue | 401 Unauthorized, ErrorCode A002 (INVALID_TOKEN) | 에러 응답 body 확인 |
| UA-003 | 만료된 refreshToken으로 재발급 | refreshToken 만료 | POST /auth/reissue | 401 Unauthorized, ErrorCode A003 (EXPIRED_TOKEN) | 에러 응답 확인 |
| UA-004 | 유효하지 않은 refreshToken | 조작된/잘못된 토큰 | POST /auth/reissue | 401 Unauthorized | 재발급 거부 확인 |

### 1.2 로그아웃

| TC-ID | 시나리오 | 사전조건 | 입력 | 기대결과 | 판정기준 |
|-------|----------|----------|------|----------|----------|
| UA-005 | 정상 로그아웃 | JWT 유효 (MEMBER) | POST /auth/logout | 204 No Content, accessToken/refreshToken 쿠키 삭제 | 쿠키 삭제 확인, DB refreshToken 삭제 확인 |
| UA-006 | 미인증 로그아웃 | 로그인 안 함 | POST /auth/logout | 401 Unauthorized | A001 (UNAUTHORIZED) |

### 1.3 OAuth2 카카오 로그인

| TC-ID | 시나리오 | 사전조건 | 입력 | 기대결과 | 판정기준 |
|-------|----------|----------|------|----------|----------|
| UA-007 | 카카오 로그인 성공 | 카카오 동의 완료 | GET /oauth2/authorization/kakao → 리다이렉트 | 쿠키에 accessToken, refreshToken 설정 | 최종 리다이렉트 URL, 쿠키 확인 |
| UA-008 | 카카오 로그인 거부 | 사용자가 동의 취소 | OAuth2 error 콜백 | OAuth2FailureHandler 동작, 에러 페이지 리다이렉트 | 에러 처리 확인 |

---

## 2. api-user 회원 (Member)

### 2.1 내 정보 조회/수정

| TC-ID | 시나리오 | 사전조건 | 입력 | 기대결과 | 판정기준 |
|-------|----------|----------|------|----------|----------|
| UM-001 | 내 정보 조회 | MEMBER 로그인 | GET /member/me | 200 OK, MyPageResponse (닉네임, 태그 등) | 응답 필드 확인 |
| UM-002 | 내 정보 수정 | MEMBER 로그인 | PATCH /member/me (UpdateMemberRequest) | 200 OK, 수정된 MyPageResponse | nickname, tagIds 반영 확인 |
| UM-003 | 미인증 내 정보 조회 | 로그인 안 함 | GET /member/me | 401 Unauthorized | A001 |
| UM-004 | 회원 탈퇴 | MEMBER 로그인 | DELETE /member/me | 204 No Content | 회원 soft-delete 또는 상태 변경 확인 |

### 2.2 선호 태그 설정

| TC-ID | 시나리오 | 사전조건 | 입력 | 기대결과 | 판정기준 |
|-------|----------|----------|------|----------|----------|
| UM-005 | 선호 태그 설정 | MEMBER 로그인 | POST /member/me/tags (SetPreferredTagRequest) | 200 OK | PreferredTag 갱신 확인 |
| UM-006 | 태그 ID 리스트 비어있음 | MEMBER 로그인 | POST /member/me/tags (tagsId: []) | 400 Bad Request | @NotEmpty 위반 (C001) |
| UM-007 | 카테고리에 맞지 않는 태그 포함 | MEMBER 로그인 | 잘못된 tagId | 400 Bad Request | B203 (INVALID_TAG_SELECTION) |

### 2.3 온보딩

| TC-ID | 시나리오 | 사전조건 | 입력 | 기대결과 | 판정기준 |
|-------|----------|----------|------|----------|----------|
| UM-008 | 온보딩 스킵 | MEMBER 로그인 | POST /member/me/onboarding/skip | 204 No Content | member.onboardingCompleted = true 확인 |

---

## 3. api-user 콘텐츠·시리즈 조회

### 3.1 콘텐츠 상세

| TC-ID | 시나리오 | 사전조건 | 입력 | 기대결과 | 판정기준 |
|-------|----------|----------|------|----------|----------|
| UC-001 | 콘텐츠 상세 조회 | MEMBER 로그인 | GET /contents/{mediaId} | 200 OK, ContentsDetailResponse | 제목, 설명, 재생 URL 등 확인 |
| UC-002 | 존재하지 않는 mediaId | MEMBER 로그인 | GET /contents/99999 | 404 Not Found | B101 (CONTENT_NOT_FOUND) |
| UC-003 | 미인증 조회 | 로그인 안 함 | GET /contents/1 | 401 Unauthorized | A001 |

### 3.2 시리즈 상세·에피소드 목록

| TC-ID | 시나리오 | 사전조건 | 입력 | 기대결과 | 판정기준 |
|-------|----------|----------|------|----------|----------|
| UC-004 | 시리즈 상세 조회 | MEMBER 로그인 | GET /series/{mediaId} | 200 OK, SeriesDetailResponse | 시리즈 정보 확인 |
| UC-005 | 시리즈 에피소드 목록 (페이지네이션) | MEMBER 로그인 | GET /series/{mediaId}/contents?page=0&size=10 | 200 OK, PageResponse | page, size 적용 확인 |
| UC-006 | 존재하지 않는 시리즈 | MEMBER 로그인 | GET /series/99999 | 404 Not Found | B102 (SERIES_NOT_FOUND) |
| UC-007 | 에피소드 미등록 시리즈 | MEMBER 로그인 | 시리즈 있으나 에피소드 없음 | 404 Not Found | S002 (EPISODE_NOT_REGISTERED) |

---

## 4. api-user 숏폼 피드 및 이벤트

### 4.1 숏폼 피드

| TC-ID | 시나리오 | 사전조건 | 입력 | 기대결과 | 판정기준 |
|-------|----------|----------|------|----------|----------|
| US-001 | 숏폼 피드 조회 | MEMBER 로그인 | GET /short-forms?page=0&size=10 | 200 OK, PageResponse<ShortFormFeedResponse> | page≥0, size>0 유효성 |
| US-002 | page 음수 | MEMBER 로그인 | GET /short-forms?page=-1 | 400 Bad Request | C001 또는 파라미터 검증 |
| US-003 | size 0 | MEMBER 로그인 | GET /short-forms?size=0 | 400 Bad Request | 유효성 검증 |

### 4.2 숏폼 시청 이벤트

| TC-ID | 시나리오 | 사전조건 | 입력 | 기대결과 | 판정기준 |
|-------|----------|----------|------|----------|----------|
| US-004 | 시청 이벤트 기록 | MEMBER 로그인 | POST /short-forms/events (shortFormId) | 200 OK | ClickEvent 등 저장 확인 |
| US-005 | shortFormId null | MEMBER 로그인 | POST /short-forms/events (shortFormId: null) | 400 Bad Request | @NotNull 위반 (C001) |
| US-006 | 존재하지 않는 숏폼 | MEMBER 로그인 | shortFormId: 99999 | 404 Not Found | B108 (SHORT_FORM_NOT_FOUND) |

### 4.3 숏폼 CTA 클릭 이벤트

| TC-ID | 시나리오 | 사전조건 | 입력 | 기대결과 | 판정기준 |
|-------|----------|----------|------|----------|----------|
| US-007 | CTA 클릭 기록 | MEMBER 로그인 | POST /short-forms/cta (shortFormId) | 200 OK | CTA 이벤트 저장 확인 |
| US-008 | shortFormId null | MEMBER 로그인 | POST /short-forms/cta (shortFormId: null) | 400 Bad Request | @NotNull 위반 |

---

## 5. api-user 재생목록 (Playlist)

### 5.1 태그 기반 재생목록

| TC-ID | 시나리오 | 사전조건 | 입력 | 기대결과 | 판정기준 |
|-------|----------|----------|------|----------|----------|
| UP-001 | 태그별 추천 재생목록 | MEMBER 로그인 | GET /playlists/me/{tagId} | 200 OK, List<TagPlaylistResponse> | tagId>0 |
| UP-002 | 시청 기록 기반 재생목록 | MEMBER 로그인 | GET /playlists/me/history?page=0 | 200 OK, PageResponse | 페이지네이션 확인 |
| UP-003 | 추천 재생목록 | MEMBER 로그인 | GET /playlists/recommend?excludeMediaId=1&page=0&size=10 | 200 OK | excludeMediaId 적용 |
| UP-004 | 상위 태그 재생목록 | MEMBER 로그인 | GET /playlists/tags/top?excludeMediaId=1&index=0 | 200 OK | index 0~2 범위 |
| UP-005 | 특정 태그 재생목록 | MEMBER 로그인 | GET /playlists/tags/{tagId}?page=0&size=10 | 200 OK | tagId 유효성 |
| UP-006 | source 누락 | MEMBER 로그인 | source 필수인데 미제공 | 400 Bad Request | B206 (INVALID_PLAYLIST_SOURCE) |

### 5.2 트렌딩·히스토리·북마크·검색 재생목록

| TC-ID | 시나리오 | 사전조건 | 입력 | 기대결과 | 판정기준 |
|-------|----------|----------|------|----------|----------|
| UP-007 | 트렌딩 재생목록 | MEMBER 로그인 | GET /playlists/trending | 200 OK | 정렬/데이터 확인 |
| UP-008 | 시청 기록 재생목록 | MEMBER 로그인 | GET /playlists/history | 200 OK | watch history 기반 |
| UP-009 | 북마크 재생목록 | MEMBER 로그인 | GET /playlists/bookmarks | 200 OK | bookmark 기반 |
| UP-010 | 검색 재생목록 | MEMBER 로그인 | GET /playlists/search?excludeMediaId=1 (필수) | 200 OK | excludeMediaId 필수 확인 |
| UP-011 | 시리즈 전용 API 위반 | MEMBER 로그인 | 시리즈 콘텐츠로 일반 playlist API 호출 | 400 Bad Request | B405 (INVALID_REQUEST_FOR_SERIES_PLAYLIST) |

---

## 6. api-user 재생·시청기록·북마크·좋아요

### 6.1 재생 위치 (Playback)

| TC-ID | 시나리오 | 사전조건 | 입력 | 기대결과 | 판정기준 |
|-------|----------|----------|------|----------|----------|
| UPL-001 | 재생 위치 저장 | MEMBER 로그인 | PUT /playback (mediaId, positionSec) | 200 OK | Playback 엔티티 갱신 |
| UPL-002 | mediaId/positionSec null | MEMBER 로그인 | 필수 필드 누락 | 400 Bad Request | @NotNull 위반 |
| UPL-003 | 존재하지 않는 mediaId | MEMBER 로그인 | mediaId: 99999 | 404 Not Found | B105 (MEDIA_NOT_FOUND) |

### 6.2 시청 기록 (Watch History)

| TC-ID | 시나리오 | 사전조건 | 입력 | 기대결과 | 판정기준 |
|-------|----------|----------|------|----------|----------|
| UWH-001 | 시청 기록 저장 | MEMBER 로그인 | PUT /watch-history (mediaId) | 200 OK | WatchHistory 갱신 |
| UWH-002 | mediaId null | MEMBER 로그인 | mediaId 누락 | 400 Bad Request | @NotNull 위반 |

### 6.3 북마크

| TC-ID | 시나리오 | 사전조건 | 입력 | 기대결과 | 판정기준 |
|-------|----------|----------|------|----------|----------|
| UB-001 | 북마크 추가 | MEMBER 로그인 | POST /bookmarks (mediaId) | 200 OK | Bookmark 생성 |
| UB-002 | 내 북마크 콘텐츠 목록 | MEMBER 로그인 | GET /bookmarks/me/contents?page=0&size=10 | 200 OK | PageResponse, size 0~100 |
| UB-003 | 내 북마크 숏폼 목록 | MEMBER 로그인 | GET /bookmarks/me/short-form | 200 OK | PageResponse |
| UB-004 | mediaId @Positive 위반 | MEMBER 로그인 | mediaId: 0 또는 음수 | 400 Bad Request | @Positive 위반 |

### 6.4 좋아요

| TC-ID | 시나리오 | 사전조건 | 입력 | 기대결과 | 판정기준 |
|-------|----------|----------|------|----------|----------|
| UL-001 | 좋아요 토글 | MEMBER 로그인 | POST /likes (mediaId) | 200 OK | Likes 추가/제거 확인 |
| UL-002 | mediaId null | MEMBER 로그인 | mediaId 누락 | 400 Bad Request | @NotNull 위반 |

---

## 7. api-user 댓글 (Comment)

### 7.1 댓글 CRUD

| TC-ID | 시나리오 | 사전조건 | 입력 | 기대결과 | 판정기준 |
|-------|----------|----------|------|----------|----------|
| UCM-001 | 댓글 작성 | MEMBER 로그인 | POST /comments (CreateCommentRequest) | 200 OK, CommentResponse | content @NotBlank, max 100 |
| UCM-002 | 댓글 수정 | MEMBER 로그인 (본인 댓글) | PATCH /comments/{commentId} (UpdateCommentRequest) | 200 OK | 본인 댓글만 수정 가능 |
| UCM-003 | 타인 댓글 수정 시도 | MEMBER 로그인 (타인 댓글) | PATCH /comments/{commentId} | 403 Forbidden | B205 (COMMENT_FORBIDDEN) |
| UCM-004 | 댓글 삭제 | MEMBER 로그인 (본인 댓글) | DELETE /comments/{commentId} | 204 No Content | 삭제 확인 |
| UCM-005 | 타인 댓글 삭제 시도 | MEMBER 로그인 (타인 댓글) | DELETE /comments/{commentId} | 403 Forbidden | B205 |
| UCM-006 | content 100자 초과 | MEMBER 로그인 | content 길이 101자 | 400 Bad Request | @Size 위반 |
| UCM-007 | content 빈 문자열 | MEMBER 로그인 | content: "" | 400 Bad Request | @NotBlank 위반 |
| UCM-008 | 존재하지 않는 댓글 | MEMBER 로그인 | commentId: 99999 | 404 Not Found | B106 (COMMENT_NOT_FOUND) |

### 7.2 댓글 목록 조회

| TC-ID | 시나리오 | 사전조건 | 입력 | 기대결과 | 판정기준 |
|-------|----------|----------|------|----------|----------|
| UCM-009 | 내 댓글 목록 | MEMBER 로그인 | GET /comments/me?page=0&size=10 | 200 OK | PageResponse<MyCommentResponse> |
| UCM-010 | 콘텐츠별 댓글 목록 | MEMBER 로그인 | GET /comments/{contentsId}/comments?includeSpoiler=true | 200 OK | includeSpoiler 적용 확인 |

---

## 8. api-user 태그·카테고리

### 8.1 태그 랭킹

| TC-ID | 시나리오 | 사전조건 | 입력 | 기대결과 | 판정기준 |
|-------|----------|----------|------|----------|----------|
| UT-001 | 태그 랭킹 조회 | MEMBER 로그인 | GET /tag/me/ranking | 200 OK, TagRankingResponse | 데이터 구조 확인 |
| UT-002 | 태그별 월간 비교 | MEMBER 로그인 | GET /tag/me/ranking/{tagId} | 200 OK | tagId>0 |
| UT-003 | 존재하지 않는 tagId | MEMBER 로그인 | tagId: 99999 | 404 Not Found | B104 (TAG_NOT_FOUND) |

### 8.2 카테고리

| TC-ID | 시나리오 | 사전조건 | 입력 | 기대결과 | 판정기준 |
|-------|----------|----------|------|----------|----------|
| UCAT-001 | 카테고리 목록 조회 | MEMBER 로그인 | GET /categories | 200 OK, List<CategoryResponse> | 카테고리 목록 확인 |
| UCAT-002 | 카테고리별 태그 조회 | MEMBER 로그인 | GET /categories/{categoryId}/tags | 200 OK | categoryId>0 |
| UCAT-003 | 존재하지 않는 categoryId | MEMBER 로그인 | categoryId: 99999 | 404 Not Found | B103 (CATEGORY_NOT_FOUND) |

---

## 9. api-admin 인증 (Back Office Auth)

### 9.1 로그인

| TC-ID | 시나리오 | 사전조건 | 입력 | 기대결과 | 판정기준 |
|-------|----------|----------|------|----------|----------|
| AA-001 | 정상 로그인 | 유효한 ADMIN/EDITOR 계정 | POST /back-office/login (email, password) | 200 OK, AdminLoginResponse, 쿠키 설정 | accessToken/refreshToken 쿠키 확인 |
| AA-002 | 잘못된 비밀번호 | DB에 존재하는 이메일 | password 틀림 | 401 Unauthorized | 로그인 실패 |
| AA-003 | 존재하지 않는 이메일 | 미가입 이메일 | email: unknown@test.com | 401 Unauthorized | 로그인 실패 |
| AA-004 | email @NotBlank 위반 | - | email: "" | 400 Bad Request | C001, errors 필드 확인 |
| AA-005 | email @Email 형식 위반 | - | email: "invalid-email" | 400 Bad Request | 유효성 검증 |
| AA-006 | password @NotBlank 위반 | - | password: "" | 400 Bad Request | C001 |

### 9.2 토큰 재발급·로그아웃

| TC-ID | 시나리오 | 사전조건 | 입력 | 기대결과 | 판정기준 |
|-------|----------|----------|------|----------|----------|
| AA-007 | refreshToken으로 재발급 | ADMIN/EDITOR 로그인 후 | POST /back-office/reissue (쿠키) | 204 No Content | 쿠키 갱신 확인 |
| AA-008 | refreshToken 없이 재발급 | 쿠키 없음 | POST /back-office/reissue | 401 Unauthorized | A002 (INVALID_TOKEN) |
| AA-009 | 로그아웃 | ADMIN/EDITOR 로그인 | POST /back-office/logout | 204 No Content | 쿠키 삭제, DB refreshToken 삭제 |

---

## 10. api-admin 업로드 (Presigned URL)

콘텐츠·시리즈·숏폼 메타데이터 업로드 시 Presigned URL 발급 검증

### 10.1 콘텐츠 업로드 (Presigned URL 발급)

| TC-ID | 시나리오 | 사전조건 | 입력 | 기대결과 | 판정기준 |
|-------|----------|----------|------|----------|----------|
| AU-001 | 콘텐츠 업로드 URL 발급 | ADMIN 로그인 | POST /back-office/admin/contents/upload (ContentsUploadRequest) | 200 OK, posterUploadUrl, thumbnailUploadUrl, originUploadUrl 3개 | IngestJob INIT, 응답에 URL 포함 |
| AU-002 | EDITOR 권한으로 업로드 | EDITOR 로그인 | 동일 요청 | 200 OK | ADMIN과 동일 허용 (admin/** 경로) |
| AU-003 | 미인증 업로드 요청 | 로그인 안 함 | ContentsUploadRequest | 401 Unauthorized | IngestJob 생성 안 됨 |
| AU-004 | title 누락 | ADMIN 로그인 | @NotBlank 필드 비움 | 400 Bad Request | C001, errors 확인 |
| AU-005 | tagIdList 비어있음 | ADMIN 로그인 | tagIdList: [] | 400 Bad Request | @NotEmpty 위반 |
| AU-006 | categoryId 0 또는 음수 | ADMIN 로그인 | categoryId: 0 | 400 Bad Request | @Positive 위반 |
| AU-007 | 지원하지 않는 이미지 확장자 | ADMIN 로그인 | posterFileName: "poster.exe" | 400 Bad Request | B301 (UNSUPPORTED_IMAGE_EXTENSION) |
| AU-008 | 지원하지 않는 동영상 확장자 | ADMIN 로그인 | originFileName: "origin.avi" | 400 Bad Request | B302 (UNSUPPORTED_VIDEO_EXTENSION) |
| AU-009 | seriesId 없이 시리즈 연결 요구 | ADMIN 로그인 | seriesId: null, 시리즈 연동 필요 시 | 400 또는 B404 | 비즈니스 규칙에 따른 검증 |

### 10.2 시리즈 업로드

| TC-ID | 시나리오 | 사전조건 | 입력 | 기대결과 | 판정기준 |
|-------|----------|----------|------|----------|----------|
| AU-010 | 시리즈 업로드 URL 발급 | ADMIN 로그인 | POST /back-office/admin/series/upload | 200 OK, posterUploadUrl, thumbnailUploadUrl | 시리즈 메타 생성, Presigned URL 반환 |
| AU-011 | 필수값 누락 | ADMIN 로그인 | title 등 필수 필드 누락 | 400 Bad Request | 유효성 검증 |

### 10.3 숏폼 업로드

| TC-ID | 시나리오 | 사전조건 | 입력 | 기대결과 | 판정기준 |
|-------|----------|----------|------|----------|----------|
| AU-012 | 숏폼 업로드 URL 발급 | EDITOR 로그인 | POST /back-office/short-forms/upload (ShortFormUploadRequest) | 200 OK, posterUploadUrl, thumbnailUploadUrl, originUploadUrl | IngestJob INIT 확인 |
| AU-013 | originId + mediaType (SERIES/CONTENTS) | EDITOR 로그인 | originId: 1, mediaType: SERIES | 200 OK | 원본 미디어 타입 검증 |
| AU-014 | 시리즈에 속한 콘텐츠를 숏폼 원본으로 선택 | EDITOR 로그인 | 잘못된 originId/mediaType 조합 | 400 Bad Request | B404 (INVALID_SHORTFORM_CONTENTS_TARGET) |
| AU-015 | seriesId와 contentsId 둘 다 제공 | EDITOR 로그인 | 둘 다 설정 | 400 Bad Request | B403 (INVALID_SHORTFORM_TARGET) |
| AU-016 | 원본 미디어 미존재 | EDITOR 로그인 | originId: 99999 | 404 Not Found | B402 (SHORTFORM_ORIGIN_MEDIA_NOT_FOUND) |

### 10.4 S3 Presigned URL 업로드 (클라이언트 측)

| TC-ID | 시나리오 | 사전조건 | 입력 | 기대결과 | 판정기준 |
|-------|----------|----------|------|----------|----------|
| AS3-001 | Presigned URL로 원본 영상 업로드 | AU-001/AU-012 성공 | mp4 파일 PUT | 200 OK | S3에 파일 존재 확인 |
| AS3-002 | 만료된 Presigned URL로 업로드 | URL 만료 후 | mp4 파일 PUT | 403 Forbidden | S3 업로드 거부 |
| AS3-003 | Content-Type 불일치 | Presigned URL (video/mp4) | .exe 파일 PUT | S3 오류 | Content-Type 검증 |

> **참고:** 업로드 완료 후 큐 발행은 S3 ObjectCreated → Lambda → SQS로 처리됩니다. 별도 API 콜백이 없는 경우, 트랜스코딩 파이프라인 테스트는 [OTT Transcoding System Test Document](./OTT-Transcoding-System-Test-Document.md) 참조.

---

## 11. api-admin 조회 및 관리

### 11.1 콘텐츠·시리즈·숏폼 목록·상세

| TC-ID | 시나리오 | 사전조건 | 입력 | 기대결과 | 판정기준 |
|-------|----------|----------|------|----------|----------|
| AG-001 | 콘텐츠 목록 조회 | ADMIN 로그인 | GET /back-office/admin/contents?page=0&size=10 | 200 OK | PageResponse |
| AG-002 | 콘텐츠 검색어·공개상태 필터 | ADMIN 로그인 | ?searchWord=테스트&publicStatus=PUBLIC | 200 OK | 필터 적용 확인 |
| AG-003 | 콘텐츠 상세 | ADMIN 로그인 | GET /back-office/admin/contents/{mediaId} | 200 OK | ContentsDetailResponse |
| AG-004 | 시리즈 목록·상세 | ADMIN 로그인 | GET /back-office/admin/series | 200 OK | 동일 형식 |
| AG-005 | 숏폼 목록·상세 | EDITOR 로그인 | GET /back-office/short-forms, GET /{mediaId} | 200 OK | ShortFormListResponse |
| AG-006 | 원본 미디어 목록 (숏폼용) | EDITOR 로그인 | GET /back-office/short-forms/origin-media | 200 OK | OriginMediaTitleListResponse |

### 11.2 콘텐츠·시리즈·숏폼 수정 (PATCH)

| TC-ID | 시나리오 | 사전조건 | 입력 | 기대결과 | 판정기준 |
|-------|----------|----------|------|----------|----------|
| AG-007 | 콘텐츠 수정 | ADMIN 로그인 | PATCH /back-office/admin/contents/{contentsId}/upload | 200 OK | ContentsUpdateResponse, 필요 시 Presigned URL |
| AG-008 | 시리즈 수정 | ADMIN 로그인 | PATCH /back-office/admin/series/{seriesId}/upload | 200 OK | Presigned URL (포스터/썸네일 교체 시) |
| AG-009 | 숏폼 수정 | EDITOR 로그인 | PATCH /back-office/short-forms/{shortformId}/upload | 200 OK | ShortFormUpdateResponse |

### 11.3 태그·카테고리·IngestJob

| TC-ID | 시나리오 | 사전조건 | 입력 | 기대결과 | 판정기준 |
|-------|----------|----------|------|----------|----------|
| AG-010 | 카테고리별 태그 통계 | ADMIN 로그인 | GET /back-office/admin/tags/stats/{categoryId} | 200 OK | TagViewResponse |
| AG-011 | 카테고리 목록 | ADMIN 로그인 | GET /back-office/admin/categories | 200 OK | CategoryListResponse |
| AG-012 | IngestJob 목록 | EDITOR 로그인 | GET /back-office/ingest-jobs?page=0&size=10 | 200 OK | IngestJobListResponse |
| AG-013 | IngestJob 검색 | EDITOR 로그인 | ?searchWord=콘텐츠명 | 200 OK | 검색 필터 적용 |

### 11.4 회원 관리 (ADMIN 전용)

| TC-ID | 시나리오 | 사전조건 | 입력 | 기대결과 | 판정기준 |
|-------|----------|----------|------|----------|----------|
| AG-014 | 회원 목록 조회 | ADMIN 로그인 | GET /back-office/admin/members | 200 OK | MemberListResponse |
| AG-015 | 회원 역할 변경 | ADMIN 로그인 | PATCH /back-office/admin/members/{memberId}/role (role) | 204 No Content | role: EDITOR, SUSPENDED 등 |
| AG-016 | EDITOR가 회원 목록 접근 | EDITOR 로그인 | GET /back-office/admin/members | 403 Forbidden | A004 (FORBIDDEN) |
| AG-017 | 허용되지 않는 역할 변경 | ADMIN 로그인 | role: 잘못된 값 | 400 Bad Request | B204 (INVALID_ROLE_CHANGE) |

---

## 12. 공통 예외 처리 및 응답 포맷

### 12.1 예외 → HTTP 매핑

| TC-ID | 시나리오 | Exception | 기대 HTTP | 기대 ErrorCode |
|-------|----------|-----------|-----------|----------------|
| EX-001 | @Valid 실패 | MethodArgumentNotValidException | 400 | C001 (INVALID_INPUT) |
| EX-002 | 필수 파라미터 누락 | MissingServletRequestParameterException | 400 | C002 (MISSING_PARAMETER) |
| EX-003 | 잘못된 타입 | MethodArgumentTypeMismatchException | 400 | C003 (INVALID_TYPE) |
| EX-004 | JSON 파싱 오류 | HttpMessageNotReadableException (JSON) | 400 | C005 (JSON_PARSE_ERROR) |
| EX-005 | 요청 본문 없음 | HttpMessageNotReadableException (기타) | 400 | C004 (MISSING_BODY) |
| EX-006 | 404 핸들러 없음 | NoHandlerFoundException | 404 | C006 (RESOURCE_NOT_FOUND) |
| EX-007 | 지원 안 하는 메서드 | HttpRequestMethodNotSupportedException | 405 | C007 (METHOD_NOT_ALLOWED) |
| EX-008 | BusinessException | BusinessException | ErrorCode에 따름 | A001, B101 등 |
| EX-009 | 그 외 예외 | Exception | 500 | C999 (INTERNAL_ERROR) |

### 12.2 응답 포맷 검증

| TC-ID | 시나리오 | 기대응답 구조 | 판정기준 |
|-------|----------|---------------|----------|
| RF-001 | 성공 응답 | `{ "success": true, "data": { ... } }` | success, data 필드 확인 |
| RF-002 | 에러 응답 | `{ "success": false, "code": "C001", "message": "...", "status": 400, "timestamp": "...", "errors": [...] }` | code, message, errors(유효성 실패 시) 확인 |
| RF-003 | 페이지네이션 | `{ "content": [...], "page": 0, "size": 10, "totalElements": N, "totalPages": M }` | PageResponse 구조 확인 |
| RF-004 | 204 No Content | Body 없음 | Content-Length 0 또는 body 비어있음 |

### 12.3 인증 실패 시나리오

| TC-ID | 시나리오 | 기대결과 | 판정기준 |
|-------|----------|----------|----------|
| AF-001 | Authorization 헤더 없음 | 401 Unauthorized | A001 (UNAUTHORIZED) |
| AF-002 | 만료된 accessToken | 401 Unauthorized | A003 (EXPIRED_TOKEN) |
| AF-003 | 잘못된 JWT 서명 | 401 Unauthorized | A002 (INVALID_TOKEN) |
| AF-004 | MEMBER가 /back-office/admin/** 접근 | 401 또는 403 | 역할 기반 거부 |
| AF-005 | EDITOR가 /back-office/admin/members 접근 | 403 Forbidden | A004 (FORBIDDEN) |

---

## 13. API Load Test

### 13.1 테스트 환경

| 항목 | 사양 |
|------|------|
| api-user | Spring Boot, 포트 8080 |
| api-admin | Spring Boot, 포트 8081 |
| DB | MySQL 8.0 |
| 부하 도구 | k6, JMeter, Artillery 등 |

### 13.2 동시 요청 테스트

| TC-ID | 시나리오 | 조건 | 측정 항목 | 합격 기준 |
|-------|----------|------|-----------|-----------|
| AL-001 | 동시 10건 콘텐츠 조회 | GET /contents/{id} × 10 | 응답 시간, 에러율 | P95 < 500ms, 에러율 0% |
| AL-002 | 동시 50건 피드 조회 | GET /short-forms × 50 | 응답 시간, DB 커넥션 | P99 < 1s |
| AL-003 | 동시 20건 재생목록 조회 | GET /playlists/recommend × 20 | 메모리, CPU | OOM 없음 |
| AL-004 | 동시 5건 Presigned URL 발급 | POST /upload × 5 (admin) | 응답 시간 | 전체 200 OK |

### 13.3 페이지네이션 부하

| TC-ID | 시나리오 | 조건 | 기대결과 |
|-------|----------|------|----------|
| AL-005 | 대용량 페이지 요청 | size=100, page=999 | 200 OK 또는 빈 결과 |
| AL-006 | 연속 페이지 요청 | page 0→1→2... 100회 | 순차 응답, 데이터 정합성 |

### 13.4 측정 지표 요약

| 지표 | 설명 | 측정 방법 |
|------|------|-----------|
| 응답 시간 (P50, P95, P99) | 요청~응답 완료 | 부하 도구 리포트 |
| 에러율 | 4xx/5xx 비율 | 로그 또는 도구 |
| RPS | 초당 요청 수 | 부하 도구 |
| DB 커넥션 수 | 동시 연결 | DB 모니터링 |
| JVM 메모리 | heap 사용량 | actuator/metrics |

---

## Appendix: API Base URL 및 역할

| API | 포트 | Base URL | 인증 |
|-----|------|----------|------|
| api-user | 8080 | http://localhost:8080 | JWT (Cookie), OAuth2 카카오 |
| api-admin | 8081 | http://localhost:8081 | JWT (Cookie), 이메일/비밀번호 |

### 역할별 접근 권한

| 경로 | ADMIN | EDITOR | MEMBER |
|------|-------|--------|--------|
| /auth/reissue | - | - | 불필요 (public) |
| /member/** | - | - | ✅ |
| /contents/** | - | - | ✅ |
| /back-office/login | - | - | 불필요 (public) |
| /back-office/admin/** | ✅ | ✅* | ❌ |
| /back-office/admin/members | ✅ | ❌ | ❌ |
| /back-office/short-forms/** | ✅ | ✅ | ❌ |
| /back-office/ingest-jobs/** | ✅ | ✅ | ❌ |

\* EDITOR: /back-office/admin/members 제외
