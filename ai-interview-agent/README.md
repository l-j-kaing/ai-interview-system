# ai-interview-agent

FastAPI + LangGraph 面试 Agent 服务骨架。面试业务流程和 Java 服务对接目前均为占位实现。

## 目录结构

```text
app/
  api/          HTTP 路由与 Pydantic 模型
  agents/       LangGraph 工作流、状态、节点、提示词和工具
  clients/      外部服务客户端
  services/     业务编排
  core/         全局配置
  utils/        公共工具
  main.py       FastAPI 启动入口
tests/          测试
.env.example    环境变量模板
requirements.txt
```

## 启动

```powershell
py -m venv .venv
.\.venv\Scripts\Activate.ps1
python -m pip install -r requirements.txt
Copy-Item .env.example .env
python -m uvicorn app.main:app --reload
```

- 健康检查：`GET /health`
- 面试接口占位：`POST /api/interview/start`（返回 `501 Not Implemented`）
- API 文档：`/docs`

运行测试：

```powershell
python -m unittest discover -s tests
```
