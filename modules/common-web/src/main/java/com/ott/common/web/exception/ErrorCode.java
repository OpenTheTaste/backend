package com.ott.common.web.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 코드 체계:
 * - C0XX: Common (공통)
 * - A0XX: Auth (인증/인가)
 * - U0XX: User (사용자)
 * - B0XX: Video/Content 등 비즈니스 룰
 * - S0XX: Server (서버)
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ========== Common (C) - 공통 ==========
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "C001", "입력값이 올바르지 않습니다"),
    MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "C002", "필수 파라미터가 없습니다"),
    INVALID_TYPE(HttpStatus.BAD_REQUEST, "C003", "타입이 올바르지 않습니다"),
    MISSING_BODY(HttpStatus.BAD_REQUEST, "C004", "요청 본문이 없습니다"),
    JSON_PARSE_ERROR(HttpStatus.BAD_REQUEST, "C005", "JSON 형식이 올바르지 않습니다"),

    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "C006", "리소스를 찾을 수 없습니다"),

    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C007", "허용되지 않은 메서드입니다"),

    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C999", "서버 오류가 발생했습니다"),

    // ========== Auth (A) - 인증/인가 ==========
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "인증이 필요합니다"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A002", "유효하지 않은 토큰입니다"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A003", "만료된 토큰입니다"),

    FORBIDDEN(HttpStatus.FORBIDDEN, "A004", "접근 권한이 없습니다"),

    // ========== User (U) - 사용자 ==========
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다"),

    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "U002", "이미 존재하는 이메일입니다"),

    // ========== Business (B) - 비즈니스 ==========
    CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "B001", "콘텐츠를 찾을 수 없습니다"),
    SERIES_NOT_FOUND(HttpStatus.NOT_FOUND, "B002", "시리즈를 찾을 수 없습니다"),
    SEARCH_KEYWORD_TOO_SHORT(HttpStatus.BAD_REQUEST, "B003", "검색어는 최소 2글자 이상이어야 합니다"),
    INVALID_ROLE_CHANGE(HttpStatus.BAD_REQUEST, "B004", "허용되지 않는 역할 변경입니다"),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "B005", "카테고리를 찾을 수 없습니다."),
    TAG_NOT_FOUND(HttpStatus.NOT_FOUND, "B006", "태그를 찾을 수 없습니다."),
    MEDIA_NOT_FOUND(HttpStatus.NOT_FOUND, "B007", "미디어를 찾을 수 없습니다."),
    BOOKMARK_NOT_FOUND(HttpStatus.NOT_FOUND, "B008", "북마크를 찾을 수 없습니다")

    UNSUPPORTED_IMAGE_EXTENSION(HttpStatus.BAD_REQUEST, "B009", "지원하지 않는 이미지 확장자입니다."),
    UNSUPPORTED_VIDEO_EXTENSION(HttpStatus.BAD_REQUEST, "B0010", "지원하지 않는 동영상 확장자입니다."),
    INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "B0011", "파일 확장자가 올바르지 않습니다."),
    INVALID_TAG_CATEGORY(HttpStatus.NOT_FOUND, "B012", "유효한 카테고리를 찾을 수 없습니다."),
    INVALID_TAG_SELECTION(HttpStatus.BAD_REQUEST, "B013", "카테고리에 맞지 않거나 존재하지 않는 태그가 포함되어 있습니다."),
    DUPLICATE_TAG_IN_LIST(HttpStatus.BAD_REQUEST, "B014", "태그 목록에 중복된 값이 있습니다."),
    INVALID_SHORTFORM_TARGET(HttpStatus.BAD_REQUEST, "B015", "seriesId와 contentsId 중 하나만 제공해야 합니다."),
    INVALID_SHORTFORM_CONTENTS_TARGET(HttpStatus.BAD_REQUEST, "B016", "시리즈에 속한 콘텐츠는 숏폼 원본으로 선택할 수 없습니다."),
    SHORTFORM_ORIGIN_MEDIA_NOT_FOUND(HttpStatus.NOT_FOUND, "B017", "숏폼 원본 미디어를 찾을 수 없습니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
