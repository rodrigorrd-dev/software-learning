from pydantic_settings import BaseSettings
from pydantic import Field

class Settings(BaseSettings):
    OPENROUTER_API_KEY: str = Field(..., description="OpenRouter API key")
    OPENROUTER_MODEL: str = "deepseek/deepseek-r1:free"
    OCR_SPACE_API_KEY: str = "helloworld"

    class Config:
        env_file = ""   # carrega .env automaticamente (raiz do projeto)
        extra = "ignore"

settings = Settings()
