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
    SERIES_NOT_FOUND(HttpStatus.NOT_FOUND, "B002", "시리즈를 찾을 수 없습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
