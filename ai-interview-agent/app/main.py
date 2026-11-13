from contextlib import asynccontextmanager

from fastapi import FastAPI

from app.agents.interview.graph import build_interview_graph
from app.core.checkpointer import create_checkpointer
from app.api.routes import interview_router, knowledge_router
from app.core.redis import redis_client
@asynccontextmanager
async def lifespan(app: FastAPI):

    try:
        # 1. 检查普通 Redis 客户端连接
        await redis_client.ping()
        app.state.redis_client = redis_client
        app.state.redis = redis_client
        print("Redis 客户端连接成功")

        # 2. 创建并管理 LangGraph Redis Checkpointer 生命周期
        with create_checkpointer() as checkpointer:

            # 3. 首次启动时初始化 Redis 索引
            checkpointer.setup()

            # 4. 创建带 Redis 持久化能力的 Graph
            app.state.interview_graph = build_interview_graph(
                checkpointer
            )

            print("LangGraph Redis Checkpointer 初始化成功")

            # 5. FastAPI 正常运行
            yield

        # 6. Checkpointer 退出 with，释放资源
        print("LangGraph Redis Checkpointer 已关闭")

    finally:
        # 7. 关闭普通 Redis 客户端
        await redis_client.aclose()
        print("Redis 客户端已关闭")





app = FastAPI(title="AI 面试 Agent 服务"
              ,lifespan=lifespan)
app.include_router(interview_router)
app.include_router(knowledge_router)
