from typing import List, Optional

from pydantic import Field, field_validator
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    cors_allow_origins: List[str] = Field(
        default_factory=list,
        env="AI_CORS_ALLOW_ORIGINS"
    )
    model_path: Optional[str] = None

    model_config = SettingsConfigDict(
        env_prefix="AI_",
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore",
        protected_namespaces=("settings_",),  # model_ 경고 제거
    )

    @field_validator("emotion_tag_pool", mode="before")
    def _parse_tags(cls, v):
        if v in (None, "", []):
            return []
        if isinstance(v, str):
            return [item.strip() for item in v.split(",") if item.strip()]
        return v

    @field_validator("cors_allow_origins", mode="before")
    def _parse_origins(cls, value):
        if isinstance(value, str):
            return [origin.strip() for origin in value.split(",") if origin.strip()]
        return value


settings = Settings()
