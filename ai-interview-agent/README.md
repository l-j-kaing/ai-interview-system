# ai-interview-agent

FastAPI + LangGraph 面试 Agent 服务。Java 服务通过 HTTP 调用本服务生成题目、评估回答并推进面试流程。

## 目录结构

```text
app/
  api/          Python Agent 的 HTTP 路由与 Pydantic 模型
  agents/       LangGraph 工作流、状态、节点、提示词和工具
  clients/      外部服务客户端
  services/     业务编排
  core/         全局配置
  utils/        公共工具
  main.py       Python Agent 启动入口
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
# 在 .env 中配置 DEEPSEEK_API_KEY 和 Redis 连接
python -m uvicorn app.main:app --reload
```

Java 服务需单独启动，并将 `python.agent.base-url` 配置为本服务地址。

- `POST /api/interview/start`：开始面试并返回首题。
- `POST /api/interview/continue`：提交回答，返回提示/追问（`CONTINUE`）、下一题及评价（`NEXT_QUESTION`），或最终评价（`FINISHED`）。
- API 文档：`http://localhost:8000/docs`

运行测试：

```powershell
python -m unittest discover -s tests
```
