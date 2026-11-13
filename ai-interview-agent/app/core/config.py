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
    deepseek_model: str = os.getenv("DEEPSEEK_MODEL", "deepseek-chat")
    deepseek_api_key: str | None = os.getenv("DEEPSEEK_API_KEY")
    deepseek_base_url: str = os.getenv(
        "DEEPSEEK_BASE_URL",
        "https://api.deepseek.com",
    )
    redis_url: str = os.getenv("REDIS_URL")

    # Checkpoint 过期时间，单位：分钟
    redis_checkpoint_ttl: int = int(
        os.getenv("REDIS_CHECKPOINT_TTL", "43200")
    )
    # 文件存储根目录
    dir_root: str = os.getenv("DIR_ROOT", "")
    # Milvus
    milvus_uri: str = os.getenv("MILVUS_URI", "")
    milvus_collection_name: str = os.getenv("MILVUS_COLLECTION_NAME", "")
    #embedding
    siliconflow_api_key: str | None = os.getenv("SILICONFLOW_API_KEY")

settings = Settings()
