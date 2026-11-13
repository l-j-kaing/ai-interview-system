"""接口路由定义。"""

from app.api.routes.interview import router as interview_router
from app.api.routes.knowledge import router as knowledge_router

__all__ = ["interview_router", "knowledge_router"]
