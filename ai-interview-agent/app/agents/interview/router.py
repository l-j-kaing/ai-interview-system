from app.agents.interview.schemas import AnswerDecision
from app.agents.interview.state import InterviewState


def route_after_evaluation(state: InterviewState) -> str:
    decision = state.get("next_action")

    answer_count = state.get("answer_times", 0)
    question_no = state.get("question_no", 0)

    # 最多允许三次正式回答
    # 达到上限后，不再允许重答或继续追问
    if answer_count >= 3:
        return "finish_question"
        

    if decision == AnswerDecision.RETRY_WITH_HINT:
        return "retry_hint"

    if decision == AnswerDecision.RETRY_WITH_GUIDANCE:
        return "retry_guidance"

    if decision == AnswerDecision.FOLLOW_UP:
        return "follow_up"

    if decision == AnswerDecision.NEXT_QUESTION:
        return "finish_question"

    if decision == AnswerDecision.FINISHED:
        return "finish_interview"

    raise ValueError(f"未知的面试决策：{decision}")


def route_after_question_evaluation(state: InterviewState) -> str:
    """
    当前正式题评价完成后，决定是否继续面试。
    """

    decision = state.get("next_action")
    question_no = state.get("question_no", 0)+1
    if decision == AnswerDecision.FINISHED or question_no > 3:
        return "end"

    return "next_question"