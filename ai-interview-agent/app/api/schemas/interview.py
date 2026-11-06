from typing import Literal

from pydantic import BaseModel, ConfigDict, Field


class InterviewStartRequest(BaseModel):
    model_config = ConfigDict(str_strip_whitespace=True)

    job_position: str = Field(min_length=1, max_length=100)
    tech_direction: str = Field(min_length=1, max_length=100)
    difficulty_mode: Literal["EASY", "MEDIUM", "HARD", "AUTO"]


class InterviewStartResponse(BaseModel):
    interview_id: str
    job_position: str
    tech_direction: str
    difficulty_mode: Literal["EASY", "MEDIUM", "HARD", "AUTO"]
    current_difficulty: Literal["EASY", "MEDIUM", "HARD"]
    status: str
    question_count: int
