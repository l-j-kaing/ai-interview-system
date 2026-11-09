from enum import Enum

from pydantic import BaseModel,Field

class DifficultyMode(str, Enum):
    AUTO = "AUTO"
    EASY = "EASY"
    MEDIUM = "MEDIUM"
    HARD = "HARD"

class Difficulty(str, Enum):
    EASY = "EASY"
    MEDIUM = "MEDIUM"
    HARD = "HARD"
#中断返回的几种状态
class InterruptStatus(str,Enum):
    CONTINUE = "CONTINUE"
    NEXT_QUESTION = "NEXT_QUESTION"
    FINISHED = "FINISHED"

class InterviewStartRequest(BaseModel):
    interview_id: str=Field(..., description="本次面试的唯一标识")
    job_position: str=Field(..., description="职位名称")
    tech_direction: str=Field(..., description="技术方向")
    difficulty_mode: DifficultyMode=Field(..., description="面试难度模式")
    current_difficulty: Difficulty=Field(..., description="当前面试题的难度")


class InterviewStartResponse(BaseModel):
    question: str = Field(..., description="AI 生成的首道面试题")
    difficulty: Difficulty=Field(..., description="当前面试题的难度")
    question_no: int = Field(..., description="当前面试题的编号")
    interview_id: str = Field(..., description="本次面试的唯一标识")


class InterviewContinueRequest(BaseModel):
    interview_id: str=Field(..., description="本次面试的唯一标识")
    answer: str=Field(...,min_length=1, description="用户输入的答案")

class InterviewContinueResponse(BaseModel):
    status: InterruptStatus
    question_no: int = Field(..., description="当前或最后完成的面试题编号")
    question: str | None = Field(None, description="当前等待回答的问题或新生成的问题")
    difficulty: Difficulty | None = Field(None, description="当前面试题的难度")
    completed_question_no: int | None = Field(None, description="已经完成的面试题编号")
    question_evaluation: dict | None = Field(None, description="已完成题目的评估结果")