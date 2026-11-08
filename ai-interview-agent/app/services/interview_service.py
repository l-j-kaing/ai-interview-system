"""面试业务编排入口。"""
from langgraph.graph.state import CompiledStateGraph
from app.api.schemas.interview import InterviewStartRequest, InterviewStartResponse

class InterviewService:

    @staticmethod
    def start_interview(data: InterviewStartRequest,
                        graph:CompiledStateGraph) -> InterviewStartResponse:

        # 1. 将请求模型转换为 LangGraph 初始输入
        input_data = {
            "interview_id": data.interview_id,
            "job_position": data.job_position,
            "tech_direction": data.tech_direction,
            "difficulty_mode": data.difficulty_mode.value,
            "current_difficulty": data.current_difficulty.value,
            "question_no": 1
        }

        # 2.配置checkpointer使用的会话标识
        config = {
            "configurable": {
                "thread_id": data.interview_id,
            }
        }
          # 3. 调用 LangGraph
        result = graph.invoke(
            input_data,
            config=config,
        )

        # 4. 将 Graph 最终结果转换为 API 响应
        return InterviewStartResponse(
            interview_id=result["interview_id"],
            question=result["current_question"],
            difficulty=result["current_difficulty"],
            question_no=result["question_no"]
        )