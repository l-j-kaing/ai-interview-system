from typing import TypedDict


class InterviewState(TypedDict, total=False):
    job_position: str
    tech_direction: str
    difficulty_mode: str
    current_difficulty: str
    messages: list[str]
