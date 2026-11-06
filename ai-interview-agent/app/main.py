from fastapi import FastAPI

from app.api.routes.interview import router as interview_router
from app.core.config import settings

app = FastAPI(title="AI 面试 Agent 服务")
app.include_router(interview_router, prefix="/api")


@app.get("/health", tags=["健康检查"])
async def health_check() -> dict[str, str]:
    return {"status": "ok"}
