from fastapi import APIRouter
from pydantic import BaseModel, Field

from app.services import recommend as recommend_service


class MoodRefreshRequest(BaseModel):
    mood: str = Field(..., min_length=1, max_length=100)
    limit: int = Field(default=5, ge=1, le=20)


class MoodRefreshResponse(BaseModel):
    items: list[str]


router = APIRouter()


@router.post("/mood-refresh", response_model=MoodRefreshResponse)
def mood_refresh(payload: MoodRefreshRequest) -> MoodRefreshResponse:
    items = recommend_service.rank_refresh(payload.mood, payload.limit)
    return MoodRefreshResponse(items=items)
