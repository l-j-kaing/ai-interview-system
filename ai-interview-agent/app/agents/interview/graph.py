from langgraph.graph import StateGraph, START, END

from app.agents.interview.nodes import evaluate_answer, generate_follow_up, generate_question, generate_question_evaluation, give_guidance, give_hint, update_interview_summary, wait_for_answer
from app.agents.interview.router import route_after_evaluation, route_after_question_evaluation
from app.agents.interview.state import InterviewState


def build_interview_graph(checkpointer):

    builder = StateGraph(InterviewState)

    # 1. 注册节点
    builder.add_node("generate_question", generate_question)
    builder.add_node("wait_for_answer", wait_for_answer)
    builder.add_node("evaluate_answer", evaluate_answer)

    builder.add_node("give_hint", give_hint)
    builder.add_node("give_guidance", give_guidance)
    builder.add_node("generate_follow_up", generate_follow_up)

    builder.add_node(
        "generate_question_evaluation",
        generate_question_evaluation,
    )
    builder.add_node("update_interview_summary", update_interview_summary)

    # 2. 首题生成流程
    builder.add_edge(START, "generate_question")
    builder.add_edge("generate_question", "wait_for_answer")

    # 3. 用户回答后的评估流程
    builder.add_edge("wait_for_answer", "evaluate_answer")

    builder.add_conditional_edges(
        "evaluate_answer",
        route_after_evaluation,
        {
            "retry_hint": "give_hint",
            "retry_guidance": "give_guidance",
            "follow_up": "generate_follow_up",
            "finish_question": "generate_question_evaluation",
            "finish_interview": "generate_question_evaluation",
        },
    )

    # 4. 提示、指导、追问后继续等待
    builder.add_edge("give_hint", "wait_for_answer")
    builder.add_edge("give_guidance", "wait_for_answer")
    builder.add_edge("generate_follow_up", "wait_for_answer")

    # 5. 更新摘要后决定是否继续面试
    builder.add_edge("generate_question_evaluation", "update_interview_summary")

    builder.add_conditional_edges(
        "update_interview_summary",
        route_after_question_evaluation,
        {
            "next_question": "generate_question",
            "end": END,
        },
    )

    # 6. 编译 Graph，注入 Checkpointer
    return builder.compile(
        checkpointer=checkpointer
    )