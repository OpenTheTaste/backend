from fastapi import APIRouter
from pydantic import BaseModel, Field

from app.services.tagging import mood_tagger
from app.errors import AppException, ErrorCode


router = APIRouter()


class ErrorItem(BaseModel):
    field: str = Field(..., description="에러가 발생한 필드")
    value: str = Field(..., description="입력된 값")
    reason: str = Field(..., description="에러 사유")


class ErrorResponse(BaseModel):
    success: bool = Field(False, description="성공 여부", examples=[False])
    code: str = Field(..., description="공통 에러 코드", examples=["C001"])
    message: str = Field(..., description="에러 메시지", examples=["입력값이 올바르지 않습니다"])
    status: int = Field(..., description="HTTP 상태 코드", examples=[400])
    timestamp: str = Field(..., description="에러 발생 시각", examples=["2026-03-12T15:30:00.123456"])
    detail: str | None = Field(None, description="상세 에러 설명", examples=["description 값이 비어 있습니다"])
    errors: list[ErrorItem] | None = Field(None, description="필드 단위 검증 에러 목록")


class TaggingRequest(BaseModel):
    media_id: int = Field(..., description="미디어 ID", examples=[101])
    description: str = Field(
        ...,
        min_length=1,
        max_length=4000,
        description="미디어 줄거리",
        examples=["가족을 잃은 주인공이 복수를 결심하고 사건의 진실을 추적하는 스릴러 이야기"],
    )

    model_config = {
        "json_schema_extra": {
            "example": {
                "media_id": 101,
                "description": "가족을 잃은 주인공이 복수를 결심하고 사건의 진실을 추적하는 스릴러 이야기"
            }
        }
    }


class TaggingResponse(BaseModel):
    media_id: int = Field(..., description="미디어 ID", examples=[101])
    mood_tags: list[str] = Field(
        ...,
        description="모델이 분류한 AI 태그 리스트",
        examples=[["긴장", "슬픔", "감동"]],
    )

    model_config = {
        "json_schema_extra": {
            "example": {
                "media_id": 101,
                "mood_tags": ["긴장", "슬픔", "감동"]
            }
        }
    }


@router.post(
    "",
    response_model=TaggingResponse,
    summary="콘텐츠 줄거리 AI 태깅",
    description="입력된 줄거리(description)를 기반으로 감정/무드 태그 상위 3개를 반환합니다.",
    responses={
        400: {
            "model": ErrorResponse,
            "description": "잘못된 요청",
            "content": {
                "application/json": {
                    "examples": {
                        "missing_parameter": {
                            "summary": "필수 파라미터 누락",
                            "value": {
                                "success": False,
                                "code": "C002",
                                "message": "필수 파라미터가 없습니다",
                                "status": 400,
                                "timestamp": "2026-03-12T15:30:00.123456",
                                "errors": [
                                    {
                                        "field": "description",
                                        "value": "",
                                        "reason": "Field required"
                                    }
                                ]
                            },
                        },
                        "invalid_input": {
                            "summary": "빈 문자열 입력",
                            "value": {
                                "success": False,
                                "code": "C001",
                                "message": "입력값이 올바르지 않습니다",
                                "status": 400,
                                "timestamp": "2026-03-12T15:30:00.123456",
                                "errors": [
                                    {
                                        "field": "description",
                                        "value": "",
                                        "reason": "String should have at least 1 character"
                                    }
                                ]
                            },
                        },
                    }
                }
            },
        },
        500: {
            "model": ErrorResponse,
            "description": "서버 내부 오류",
            "content": {
                "application/json": {
                    "example": {
                        "success": False,
                        "code": "C999",
                        "message": "서버 오류가 발생했습니다",
                        "status": 500,
                        "timestamp": "2026-03-12T15:30:00.123456",
                        "detail": "태깅 처리 중 예외가 발생했습니다"
                    }
                }
            },
        },
    },
)
def tag_text(payload: TaggingRequest) -> TaggingResponse:
    try:
        tags = mood_tagger.predict(text=payload.description, top_k=3)

        return TaggingResponse(
            media_id=payload.media_id,
            mood_tags=tags,
        )
    except AppException:
        raise
    except Exception as e:
        raise AppException(
            ErrorCode.INTERNAL_ERROR,
            detail=f"태깅 처리 중 예외가 발생했습니다: {str(e)}",
        )