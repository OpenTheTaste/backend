from fastapi import APIRouter
from pydantic import BaseModel, Field

from app.config import settings
from app.services import tagging as tagging_service


class TaggingRequest(BaseModel):
    text: str = Field(..., min_length=1, max_length=4000)


class TaggingResponse(BaseModel):
    tag_codes: list[str]
    scores: dict[str, float] | None = None


router = APIRouter()


@router.post("", response_model=TaggingResponse)
def tag_text(payload: TaggingRequest) -> TaggingResponse:
    tag_codes, scores = tagging_service.generate_tags(payload.text, settings.emotion_tag_pool)
    return TaggingResponse(tag_codes=tag_codes, scores=scores)
