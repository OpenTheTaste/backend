from typing import Annotated, List, Optional

from pydantic import Field, field_validator
from pydantic_settings import BaseSettings, NoDecode, SettingsConfigDict


class Settings(BaseSettings):
    cors_allow_origins: Annotated[List[str], NoDecode] = Field(
        default_factory=list,
        env="AI_CORS_ALLOW_ORIGINS"
    )
    tagging_model_path: str = Field(env="AI_TAGGING_MODEL_PATH")   
    # model_path: Optional[str] = Field(default=None, validation_alias="AI_MODEL_PATH")
    mood_model_path: Optional[str] = Field(default=None, validation_alias="AI_MOOD_MODEL_PATH")

    model_config = SettingsConfigDict(
        env_prefix="AI_",
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore",
        protected_namespaces=("settings_",),  # model_ 경고 제거
    )

    @field_validator("cors_allow_origins", mode="before")
    def _parse_origins(cls, value):
        if isinstance(value, str):
            return [origin.strip() for origin in value.split(",") if origin.strip()]
        return value


settings = Settings()
