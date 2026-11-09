"""面试业务编排入口。"""
from fastapi import HTTPException
from langchain_core.messages import AIMessage
from langgraph.graph.state import Command, CompiledStateGraph
from pydantic import BaseModel

from app.api.schemas.interview import (
    InterruptStatus,
    InterviewContinueRequest,
    InterviewContinueResponse,
    InterviewStartRequest,
    InterviewStartResponse,
)

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

    @staticmethod
    def continue_interview(data:InterviewContinueRequest,
                           graph:CompiledStateGraph) -> InterviewContinueResponse:
        config = {
            "configurable": {
                "thread_id": data.interview_id,
            }
        }
        checkpoint = graph.get_state(config)
        if not checkpoint.values:
            raise HTTPException(
                status_code=404,
                detail="未找到该面试会话",
            )
        if not any(task.interrupts for task in checkpoint.tasks):
            raise HTTPException(
                status_code=409,
                detail="该面试当前没有等待提交的回答",
            )

        current_question_no = checkpoint.values.get("question_no")
        if not isinstance(current_question_no, int):
            raise HTTPException(
                status_code=409,
                detail="面试状态缺少有效的当前题号",
            )

        result = graph.invoke(
            Command(resume=data.answer),
            config=config,
        )

        if result.get("__interrupt__"):
            result_question_no = result.get("question_no")
            question = result.get("current_question")
            difficulty = result.get("current_difficulty")
            if not isinstance(result_question_no, int) or not question or not difficulty:
                raise HTTPException(
                    status_code=500,
                    detail="面试流程恢复后缺少当前题目状态",
                )

            if result_question_no > current_question_no:
                evaluation = result.get("question_evaluation")
                if isinstance(evaluation, BaseModel):
                    evaluation = evaluation.model_dump(mode="json")
                if not isinstance(evaluation, dict):
                    raise HTTPException(
                        status_code=500,
                        detail="已完成题目缺少有效的评价结果",
                    )
                return InterviewContinueResponse(
                    status=InterruptStatus.NEXT_QUESTION,
                    question_no=result_question_no,
                    question=question,
                    difficulty=difficulty,
                    completed_question_no=current_question_no,
                    question_evaluation=evaluation,
                )

            messages = result.get("messages", [])
            latest_assistant_message = next(
                (
                    message
                    for message in reversed(messages)
                    if isinstance(message, AIMessage)
                ),
                None,
            )
            response_text = (
                latest_assistant_message.content
                if latest_assistant_message is not None
                else None
            )
            if not isinstance(response_text, str) or not response_text.strip():
                raise HTTPException(
                    status_code=500,
                    detail="面试流程恢复后缺少有效的提示或追问内容",
                )

            return InterviewContinueResponse(
                status=InterruptStatus.CONTINUE,
                question_no=result_question_no,
                question=response_text,
            )

        result_question_no = result.get("question_no")
        evaluation = result.get("question_evaluation")
        if isinstance(evaluation, BaseModel):
            evaluation = evaluation.model_dump(mode="json")
        if not isinstance(result_question_no, int) or not isinstance(evaluation, dict):
            raise HTTPException(
                status_code=500,
                detail="面试结束时缺少有效的题目评价结果",
            )
        return InterviewContinueResponse(
            status=InterruptStatus.FINISHED,
            question_no=result_question_no,
            question_evaluation=evaluation,
        )
