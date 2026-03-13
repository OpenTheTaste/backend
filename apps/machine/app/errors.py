from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime
from enum import Enum
from typing import Any

from fastapi import HTTPException, Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from starlette import status


@dataclass(frozen=True)
class ErrorCodeInfo:
    status: int
    code: str
    message: str


class ErrorCode(Enum):
    INVALID_INPUT = ErrorCodeInfo(status.HTTP_400_BAD_REQUEST, "C001", "입력값이 올바르지 않습니다")
    MISSING_PARAMETER = ErrorCodeInfo(status.HTTP_400_BAD_REQUEST, "C002", "필수 파라미터가 없습니다")
    INVALID_TYPE = ErrorCodeInfo(status.HTTP_400_BAD_REQUEST, "C003", "타입이 올바르지 않습니다")
    MISSING_BODY = ErrorCodeInfo(status.HTTP_400_BAD_REQUEST, "C004", "요청 본문이 없습니다")
    JSON_PARSE_ERROR = ErrorCodeInfo(status.HTTP_400_BAD_REQUEST, "C005", "JSON 형식이 올바르지 않습니다")
    RESOURCE_NOT_FOUND = ErrorCodeInfo(status.HTTP_404_NOT_FOUND, "C006", "리소스를 찾을 수 없습니다")
    METHOD_NOT_ALLOWED = ErrorCodeInfo(status.HTTP_405_METHOD_NOT_ALLOWED, "C007", "허용되지 않은 메서드입니다")
    INTERNAL_ERROR = ErrorCodeInfo(status.HTTP_500_INTERNAL_SERVER_ERROR, "C999", "서버 오류가 발생했습니다")

    UNAUTHORIZED = ErrorCodeInfo(status.HTTP_401_UNAUTHORIZED, "A001", "인증이 필요합니다")
    INVALID_TOKEN = ErrorCodeInfo(status.HTTP_401_UNAUTHORIZED, "A002", "유효하지 않은 토큰입니다")
    EXPIRED_TOKEN = ErrorCodeInfo(status.HTTP_401_UNAUTHORIZED, "A003", "만료된 토큰입니다")
    FORBIDDEN = ErrorCodeInfo(status.HTTP_403_FORBIDDEN, "A004", "접근 권한이 없습니다")

    USER_NOT_FOUND = ErrorCodeInfo(status.HTTP_404_NOT_FOUND, "U001", "사용자를 찾을 수 없습니다")

    CONTENTS_NOT_FOUND = ErrorCodeInfo(status.HTTP_404_NOT_FOUND, "B101", "콘텐츠를 찾을 수 없습니다")
    SERIES_NOT_FOUND = ErrorCodeInfo(status.HTTP_404_NOT_FOUND, "B102", "시리즈를 찾을 수 없습니다")
    CATEGORY_NOT_FOUND = ErrorCodeInfo(status.HTTP_404_NOT_FOUND, "B103", "카테고리를 찾을 수 없습니다")
    TAG_NOT_FOUND = ErrorCodeInfo(status.HTTP_404_NOT_FOUND, "B104", "태그를 찾을 수 없습니다")
    MEDIA_NOT_FOUND = ErrorCodeInfo(status.HTTP_404_NOT_FOUND, "B105", "미디어를 찾을 수 없습니다")
    COMMENT_NOT_FOUND = ErrorCodeInfo(status.HTTP_404_NOT_FOUND, "B106", "댓글을 찾을 수 없습니다")
    BOOKMARK_NOT_FOUND = ErrorCodeInfo(status.HTTP_404_NOT_FOUND, "B107", "북마크를 찾을 수 없습니다")
    EPISODE_NOT_REGISTERED = ErrorCodeInfo(status.HTTP_404_NOT_FOUND, "B108", "아직 에피소드가 등록되지 않았습니다.")
    SHORT_FORM_NOT_FOUND = ErrorCodeInfo(status.HTTP_404_NOT_FOUND, "B109", "숏폼을 찾을 수 없습니다")

    SEARCH_KEYWORD_TOO_SHORT = ErrorCodeInfo(status.HTTP_400_BAD_REQUEST, "B201", "검색어는 최소 2글자 이상이어야 합니다")
    DUPLICATE_TAG_IN_LIST = ErrorCodeInfo(status.HTTP_400_BAD_REQUEST, "B202", "태그 목록에 중복된 값이 있습니다")
    INVALID_TAG_SELECTION = ErrorCodeInfo(status.HTTP_400_BAD_REQUEST, "B203", "카테고리에 맞지 않는 태그가 포함되어 있습니다")
    INVALID_ROLE_CHANGE = ErrorCodeInfo(status.HTTP_400_BAD_REQUEST, "B204", "허용되지 않는 역할 변경입니다")
    COMMENT_FORBIDDEN = ErrorCodeInfo(status.HTTP_403_FORBIDDEN, "B205", "본인이 작성한 댓글만 수정/삭제할 수 있습니다")
    INVALID_PLAYLIST_SOURCE = ErrorCodeInfo(status.HTTP_400_BAD_REQUEST, "B206", "재생목록 소스(source)는 필수값입니다")

    STRATEGY_NOT_FOUND = ErrorCodeInfo(status.HTTP_500_INTERNAL_SERVER_ERROR, "S001", "적절한 재생목록 전략을 찾을 수 없습니다")

    @property
    def http_status(self) -> int:
        return self.value.status

    @property
    def code(self) -> str:
        return self.value.code

    @property
    def message(self) -> str:
        return self.value.message


class AppException(Exception):
    def __init__(
        self,
        error_code: ErrorCode,
        detail: str | None = None,
        errors: list[dict[str, str]] | None = None,
    ) -> None:
        super().__init__(detail or error_code.message)
        self.error_code = error_code
        self.detail = detail
        self.errors = errors


def _error_body(
    error_code: ErrorCode,
    detail: str | None = None,
    errors: list[dict[str, str]] | None = None,
) -> dict[str, Any]:
    payload: dict[str, Any] = {
        "success": False,
        "code": error_code.code,
        "message": error_code.message,
        "status": error_code.http_status,
        "timestamp": datetime.now().isoformat(),
    }
    if errors:
        payload["errors"] = errors
    if detail:
        payload["detail"] = detail
    return payload


def _map_validation_error(exc: RequestValidationError) -> ErrorCode:
    first_error = exc.errors()[0] if exc.errors() else {}
    error_type = first_error.get("type", "")
    if error_type == "missing":
        return ErrorCode.MISSING_PARAMETER
    if error_type == "json_invalid":
        return ErrorCode.JSON_PARSE_ERROR
    if error_type.startswith("type_error"):
        return ErrorCode.INVALID_TYPE
    return ErrorCode.INVALID_INPUT


def _validation_errors(exc: RequestValidationError) -> list[dict[str, str]]:
    items: list[dict[str, str]] = []
    for err in exc.errors():
        loc = ".".join(str(part) for part in err.get("loc", []) if part != "body")
        items.append(
            {
                "field": loc or "request",
                "value": "" if err.get("input") is None else str(err.get("input")),
                "reason": err.get("msg", "Invalid value"),
            }
        )
    return items


async def app_exception_handler(_: Request, exc: AppException) -> JSONResponse:
    return JSONResponse(
        status_code=exc.error_code.http_status,
        content=_error_body(exc.error_code, detail=exc.detail, errors=exc.errors),
    )


async def http_exception_handler(_: Request, exc: HTTPException) -> JSONResponse:
    mapping = {
        status.HTTP_401_UNAUTHORIZED: ErrorCode.UNAUTHORIZED,
        status.HTTP_403_FORBIDDEN: ErrorCode.FORBIDDEN,
        status.HTTP_404_NOT_FOUND: ErrorCode.RESOURCE_NOT_FOUND,
        status.HTTP_405_METHOD_NOT_ALLOWED: ErrorCode.METHOD_NOT_ALLOWED,
    }
    error_code = mapping.get(exc.status_code, ErrorCode.INTERNAL_ERROR)
    detail = exc.detail if isinstance(exc.detail, str) else None
    return JSONResponse(
        status_code=error_code.http_status,
        content=_error_body(error_code, detail=detail),
    )


async def validation_exception_handler(_: Request, exc: RequestValidationError) -> JSONResponse:
    error_code = _map_validation_error(exc)
    return JSONResponse(
        status_code=error_code.http_status,
        content=_error_body(error_code, errors=_validation_errors(exc)),
    )


async def unhandled_exception_handler(_: Request, exc: Exception) -> JSONResponse:
    return JSONResponse(
        status_code=ErrorCode.INTERNAL_ERROR.http_status,
        content=_error_body(ErrorCode.INTERNAL_ERROR, detail=str(exc)),
    )
