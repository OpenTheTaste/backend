from fastapi import APIRouter
from pydantic import BaseModel, Field

from app.services import recommend as recommend_service



# Request DTO
class MoodRefreshTargetRequest(BaseModel):
    member_id: int = Field(..., ge=1)
    input_tags: list[str] = Field(..., min_length=1, description="최근 3편의 영상에서 추출된 대표 태그 배열")

# Response DTO
class MoodRefreshTargetResponse(BaseModel):
    target_tag_codes: list[str]


router = APIRouter()


@router.post("/mood-refresh/target", response_model=MoodRefreshTargetResponse)
def target_tags(payload: MoodRefreshTargetRequest) -> MoodRefreshTargetResponse:
    # 추론 서비스 호출
    target_tags = recommend_service.infer_targets(payload.input_tags)
    
    # 결과를 담아서 반환
    return MoodRefreshTargetResponse(target_tag_codes=target_tags)