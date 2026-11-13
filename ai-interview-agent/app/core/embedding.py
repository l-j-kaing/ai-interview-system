from langchain_openai import OpenAIEmbeddings

from app.core.config import settings


embedding_model = OpenAIEmbeddings(
    model="BAAI/bge-m3",
    api_key=settings.siliconflow_api_key,
    base_url="https://api.siliconflow.cn/v1",
    check_embedding_ctx_length=False,
)