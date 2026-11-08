from enum import Enum
from pydantic import BaseModel, Field


# 回答质量和完整性等级
class EvaluationLevel(str, Enum):
    GOOD = "GOOD"
    AVERAGE = "AVERAGE"
    WEAK = "WEAK"

#题目难度
class QuestionDifficulty(str, Enum):
    EASY = "EASY"
    MEDIUM = "MEDIUM"
    HARD = "HARD"
# 本次回答之后的处理决策
class AnswerDecision(str, Enum):
    RETRY_WITH_HINT = "RETRY_WITH_HINT"
    RETRY_WITH_GUIDANCE = "RETRY_WITH_GUIDANCE"
    FOLLOW_UP = "FOLLOW_UP"
    NEXT_QUESTION = "NEXT_QUESTION"
    FINISHED = "FINISHED"


# 每次回答的过程评价
class AnswerProcessEvaluation(BaseModel):
    decision: AnswerDecision


# 一道正式题结束后的完整评价
class QuestionEvaluation(BaseModel):
    score: int = Field(ge=0, le=100)
    quality: EvaluationLevel
    completeness: EvaluationLevel

    correct_points: list[str]
    missing_points: list[str]
    error_points: list[str]

    feedback: str
    next_difficulty: QuestionDifficulty