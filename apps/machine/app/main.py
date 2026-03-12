from fastapi import FastAPI, HTTPException
from fastapi.exceptions import RequestValidationError
from fastapi.middleware.cors import CORSMiddleware

from app.api.tagging import router as tagging_router
# from app.api.recommend import router as recommend_router

from app.errors import (
    AppException,
    app_exception_handler,
    http_exception_handler,
    unhandled_exception_handler,
    validation_exception_handler,
)

tags_metadata = [
    {
        "name": "Tagging",
        "description": "콘텐츠 줄거리(description)를 기반으로 AI 감정 태그를 추출합니다.",
    },
    {
        "name": "Recommend",
        "description": "사용자 맞춤 추천 결과를 반환합니다.",
    },
]

app = FastAPI(
    title="OTT Machine API",
    summary="OTT 추천 및 태깅을 위한 AI 서버",
    description="OTT 서비스용 AI 서버입니다.",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc",
    openapi_tags=tags_metadata,
    swagger_ui_parameters={
        "displayRequestDuration": True,
        "docExpansion": "list",
        "defaultModelsExpandDepth": -1,
        "persistAuthorization": True,
    },
)

# CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 운영에서는 프론트 도메인만 허용 권장
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Exception Handlers
app.add_exception_handler(AppException, app_exception_handler)
app.add_exception_handler(HTTPException, http_exception_handler)
app.add_exception_handler(RequestValidationError, validation_exception_handler)
app.add_exception_handler(Exception, unhandled_exception_handler)

# Routers
app.include_router(tagging_router, prefix="/tagging", tags=["Tagging"])
# app.include_router(recommend_router, prefix="/recommend", tags=["Recommend"])


@app.get(
    "/health",
    summary="헬스 체크",
    description="AI 서버 상태를 확인합니다.",
)
def health() -> dict[str, str]:
    return {"status": "ok"}