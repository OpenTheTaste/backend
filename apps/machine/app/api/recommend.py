from fastapi import APIRouter
from pydantic import BaseModel, Field

from app.services import recommend as recommend_service


class MoodRefreshTargetRequest(BaseModel):
    member_id: int = Field(..., ge=1)
    recent_negative_tags: list[str] = Field(..., min_items=1)


class MoodRefreshTargetResponse(BaseModel):
    target_tag_codes: list[str]


router = APIRouter()


@router.post("/mood-refresh/target", response_model=MoodRefreshTargetResponse)
def target_tags(payload: MoodRefreshTargetRequest) -> MoodRefreshTargetResponse:
    target_tag_codes = recommend_service.predict_target_tags(payload.recent_negative_tags)
    return MoodRefreshTargetResponse(target_tag_codes=target_tag_codes)
