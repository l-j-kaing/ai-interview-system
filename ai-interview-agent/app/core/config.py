import os
from dataclasses import dataclass

from dotenv import load_dotenv

load_dotenv()


@dataclass(frozen=True)
class Settings:
    app_name: str = os.getenv("APP_NAME", "ai-interview-agent")
    java_api_base_url: str = os.getenv(
        "JAVA_API_BASE_URL",
        "http://localhost:8080",
    )


settings = Settings()
