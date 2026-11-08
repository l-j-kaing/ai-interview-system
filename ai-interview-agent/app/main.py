from contextlib import asynccontextmanager

from fastapi import FastAPI

from app.agents.interview.graph import build_interview_graph
from app.core.checkpointer import create_checkpointer
from app.api.routes import interview_router

@asynccontextmanager
async def lifespan(app: FastAPI):

    # 1. 创建并管理 Redis Checkpointer 生命周期
    with create_checkpointer() as checkpointer:

        # 2. 首次启动时初始化 Redis 索引
        checkpointer.setup()

        # 3. 创建带 Redis 持久化能力的 Graph
        app.state.interview_graph = build_interview_graph(
            checkpointer
        )

        print("LangGraph Redis Checkpointer 初始化成功")

        # 4. FastAPI 正常运行
        yield

    # 5. 应用关闭后，退出 with，释放 Redis 连接
    print("LangGraph Redis Checkpointer 已关闭")





app = FastAPI(title="AI 面试 Agent 服务"
              ,lifespan=lifespan)
app.include_router(interview_router)

