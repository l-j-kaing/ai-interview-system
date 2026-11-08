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

class InterviewStartRequest(BaseModel):
    interview_id: str
    job_position: str
    tech_direction: str
    difficulty_mode: DifficultyMode
    current_difficulty: Difficulty


class InterviewStartResponse(BaseModel):
    question: str = Field(..., description="AI 生成的首道面试题")
    difficulty: Difficulty=Field(..., description="当前面试题的难度")
    question_no: int = Field(..., description="当前面试题的编号")
    interview_id: str = Field(..., description="本次面试的唯一标识")