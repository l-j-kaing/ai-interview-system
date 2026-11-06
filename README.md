# AI Interview System

面向 AI 面试场景的项目，由 Java 后端服务与 Python Agent 服务组成，统一维护在本仓库中。

## 项目结构

```text
ai-interview-project/
├── .gitignore
├── README.md
├── ai-interview/          # Java Spring Boot 后端
│   ├── src/
│   ├── pom.xml
│   └── ...
└── ai-interview-agent/    # Python FastAPI + LangGraph Agent（待接入）
    ├── app/
    ├── requirements.txt
    └── ...
```

## 模块说明

### ai-interview（Java）

Spring Boot 后端，当前包含：

- 用户注册、登录与 JWT 鉴权
- 用户信息维护
- 知识库文件上传、持久化与向量化处理
- 基于 Spring AI 的对话模型与嵌入模型接入
- Milvus 向量库集成

技术栈：Java、Spring Boot、MyBatis、MySQL、Redis、Milvus、Spring AI、Maven。

本地启动（在 `ai-interview` 目录下，需提前安装 Maven）：

```bash
mvn spring-boot:run
```

主要配置位于 `src/main/resources/application.yaml` 与 `application-dev.yaml`。运行前需准备 MySQL、Redis、Milvus，并通过环境变量提供模型 API Key，例如：

- `DEEPSEEK_API_KEY`
- `SILICONFLOW_API_KEY`

### ai-interview-agent（Python）

规划使用 FastAPI + LangGraph 实现 Agent 服务，当前为占位目录，代码后续补充。

## 安全说明

请勿提交 `.env`、数据库密码、API Key、JWT 密钥等敏感信息。示例配置请使用 `.env.example` 等模板文件。

## 开发状态

Java 后端持续开发中；Python Agent 模块待接入。
