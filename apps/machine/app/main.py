from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.api import tagging, recommend
from app.config import settings

app = FastAPI(title="OTT AI Service", version="0.1.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_allow_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(tagging.router, prefix="/tagging", tags=["tagging"])
app.include_router(recommend.router, prefix="/recommend", tags=["recommend"])


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok"}
