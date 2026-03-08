package com.ott.transcoder.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 트랜스코더 전용 에러 코드
 *
 * T0XX: Fatal (재시도 불가)
 * T1XX: Retryable (재시도 가능)
 */
@Getter
@RequiredArgsConstructor
public enum TranscodeErrorCode {

    // ========== Fatal (T0XX) — 재시도 불가 ==========
    FILE_NOT_FOUND("T001", "파일을 찾을 수 없습니다"),
    FILE_NOT_READABLE("T002", "파일 읽기 권한이 없습니다"),
    FILE_EMPTY("T003", "파일 크기가 0입니다"),
    FILE_SIZE_EXCEEDED("T004", "파일 크기 상한을 초과했습니다"),
    INVALID_FILE_FORMAT("T005", "알 수 없는 파일 포맷입니다"),
    UNSUPPORTED_CODEC("T006", "지원하지 않는 코덱입니다"),
    INVALID_RESOLUTION("T007", "해상도가 유효하지 않습니다"),
    INVALID_DURATION("T008", "재생 시간이 유효하지 않습니다"),
    INVALID_FPS("T009", "프레임레이트가 유효하지 않습니다"),
    NO_VIDEO_STREAM("T010", "비디오 스트림이 없습니다"),

    // ========== Retryable (T1XX) — 재시도 가능 ==========
    PROBE_FAILED("T101", "미디어 분석에 실패했습니다"),
    PROBE_TIMEOUT("T102", "미디어 분석 시간이 초과되었습니다"),
    FFMPEG_FAILED("T103", "트랜스코딩에 실패했습니다"),
    FFMPEG_TIMEOUT("T104", "트랜스코딩 시간이 초과되었습니다"),
    STORAGE_FAILED("T105", "스토리지 작업에 실패했습니다"),
    DISK_SPACE_INSUFFICIENT("T106", "디스크 공간이 부족합니다")

    ;

    private final String code;
    private final String message;
}
