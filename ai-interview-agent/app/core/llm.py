from langchain_openai import ChatOpenAI

from app.core.config import settings


llm = ChatOpenAI(
    model=settings.deepseek_model,
    api_key=settings.deepseek_api_key,
    base_url=settings.deepseek_base_url,
    temperature=0.7,
    extra_body={"thinking": {"type": "disabled"}},
)