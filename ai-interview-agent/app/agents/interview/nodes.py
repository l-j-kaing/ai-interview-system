from langchain_core.messages import AIMessage, HumanMessage

from app.agents.interview.schemas import AnswerDecision, AnswerProcessEvaluation, QuestionEvaluation
from app.agents.interview.state import InterviewState
from app.core.llm import llm
from langgraph.types import interrupt

def generate_question(state: InterviewState) -> dict:
#前面的面试对话概述
    interview_summary = state.get("interview_summary", "")
#前面的对话历史
    messages = state.get("messages", [])
    recent_messages = messages[-10:]
    prompt = f"""
你是一名专业的技术面试官。

请根据以下信息生成一道技术面试题：

岗位：{state["job_position"]}
技术方向：{state["tech_direction"]}
难度：{state["current_difficulty"]}
前面的面试对话概述:
{interview_summary}
前面的对话部分最近历史：
{recent_messages}
要求：
1. 生成一道符合岗位和技术方向的面试题。
2. 难度必须符合指定等级。
3. 题目应具有实际面试价值。
4. 只输出题目，不要输出答案和解析。
"""

    response = llm.invoke(prompt)
    answer_times = state.get("answer_times", 0) 
    question = response.content.strip()

    if not question:
        raise ValueError("AI 未能生成有效的面试题")

    next_action = state.get("next_action")
    if next_action == AnswerDecision.NEXT_QUESTION or answer_times >= 3:
        return {
            "current_question": question,
            "question_no": state.get("question_no", 1)+1,
            "messages": [
                AIMessage(content=question)
            ],
            "question_times":1,
            "answer_times": 0
        }

    return {
        "current_question": question,
        "messages":[
            AIMessage(content=question)
        ],
        "question_times":state.get("question_times", 0)+1
    }





def evaluate_answer(state: InterviewState) -> dict:
    """
    评估用户本次回答，返回质量、完整性和后续决策。
    """
    #正常面试摘要
    interview_summary = state.get("interview_summary", "")
    #最近的对话历史
    messages = state.get("messages", [])
    recent_messages = messages[-10:]
    #最新的回答
    current_answer = state.get("current_answer", "")

    # 使用结构化输出，让模型按照 Pydantic 模型返回结果
    structured_llm = llm.with_structured_output(
        AnswerProcessEvaluation
    )

    prompt = f"""
你是一名专业的技术面试官，请评估用户对技术面试题的回答。
本次面试的摘要：
{interview_summary}

用户本次回答：
{current_answer}

之前的历史：
{recent_messages}

请根据以下信息，评估用户对技术面试题的回答质量、完整性和后续决策：

请从以下两个维度评估本次回答：

1. quality：回答质量
- GOOD：技术理解正确，表达清晰，核心原理准确。
- AVERAGE：基本理解，但存在不够准确或不够深入的地方。
- WEAK：存在明显错误，或者没有理解核心概念。

2. completeness：回答完整性
- GOOD：覆盖题目要求的主要知识点。
- AVERAGE：回答了部分关键内容，但仍有遗漏。
- WEAK：缺少大部分关键内容，回答明显不完整。

然后根据评估结果决定下一步：

- RETRY_WITH_HINT：回答有一定基础，只需要给出提示，让用户重新回答。
- RETRY_WITH_GUIDANCE：回答存在明显问题，需要指出错误或缺失方向。
- FOLLOW_UP：回答质量和完整性都较好，可以深入追问。
- NEXT_QUESTION：当前题目可以结束，进入下一道正式题。
- FINISHED：整场面试可以结束。

注意：
1. 不要因为用户回答较短就直接判定为 WEAK。
2. 必须结合题目要求判断回答是否正确、完整。
3. 不要编造用户没有表达的知识点。
4. 只返回规定的结构化评价结果。
"""

    result = structured_llm.invoke(prompt)

    return {
        "next_action": result.decision
    }



def wait_for_answer(state: InterviewState) -> dict:
    """
    暂停 Graph，等待用户提交答案。
    """

    answer = interrupt({
        "interview_id": state["interview_id"],
        "current_question": state["current_question"],
        "question_no": state.get("question_no"),
        "current_difficulty": state["current_difficulty"]

    })
    answer_times = state.get("answer_times", 0) + 1

    return {
       "current_answer": answer,
       "answer_times": answer_times,
       "messages": [
           HumanMessage(content=answer)
       ]
    }



def give_hint(state: InterviewState) -> dict:
    #正常面试摘要
    interview_summary = state.get("interview_summary", "")
    #最近的对话历史
    messages = state.get("messages", [])
    recent_messages = messages[-10:]
    #最新的回答
    current_answer = state.get("current_answer", "")
    prompt = f"""
你是一名专业的技术面试官。
本次面试的摘要：
{interview_summary}

最近的对话：
{recent_messages}

用户本次回答：
{current_answer}

请给用户一个适当的提示，帮助用户完善回答。

要求：
1. 不要直接给出完整答案。
2. 指出应该思考的方向。
3. 提示要简洁，避免直接泄露答案。
4. 使用中文回答。
"""

    response = llm.invoke(prompt)

    return {
        "current_question": state["current_question"],
        "messages": [
            AIMessage(content=response.content)
        ],
        "question_times": state.get("question_times")+1
    }


def give_guidance(state: InterviewState) -> dict:
    # 正常面试摘要
    interview_summary = state.get("interview_summary", "")
    # 最近的对话历史
    messages = state.get("messages", [])
    recent_messages = messages[-10:]
    # 最新的回答
    current_answer = state.get("current_answer", "")
    prompt = f"""
你是一名专业的技术面试官。

本次面试的摘要：
{interview_summary}

最近的对话：
{recent_messages}

用户本次回答：
{current_answer}

请分析用户回答中的问题，并提供改进方向。

要求：
1. 明确指出回答中存在的错误。
2. 指出遗漏的关键知识点。
3. 给出应该补充的知识方向。
4. 不要直接生成完整标准答案。
5. 使用中文回答。
"""

    response = llm.invoke(prompt)

    return {
        "current_question": state["current_question"],
        "messages": [
            AIMessage(content=response.content.strip())
        ],
        "question_times": state.get("question_times")+1
    }


def generate_follow_up(state: InterviewState) -> dict:
    # 正常面试摘要
    interview_summary = state.get("interview_summary", "")
    # 最近的对话历史
    messages = state.get("messages", [])
    recent_messages = messages[-10:]
    # 最新的回答
    current_answer = state.get("current_answer", "")
    prompt = f"""
你是一名专业的技术面试官。

本次面试的摘要：
{interview_summary}

最近的对话：
{recent_messages}


用户本次回答：
{current_answer}

请根据用户的回答，生成一道有针对性的深入追问。

要求：
1. 追问必须与当前正式题相关。
2. 不要简单重复原题。
3. 可以考察底层原理、实际应用或复杂场景。
4. 难度应与用户当前表现相匹配。
5. 只输出追问内容，不要输出答案。
6. 使用中文。
"""

    response = llm.invoke(prompt)
    follow_up_question = response.content.strip()

    return {
        "current_question": follow_up_question,
        "messages": [
            AIMessage(content=follow_up_question)
        ],
        "question_times": state.get("question_times")+1
    }


def generate_question_evaluation(state: InterviewState) -> dict:
    """
    综合当前正式题的所有回答，生成最终评价。
    """
    #对话历史次数
    times = state.get("answer_times", 0) + state.get("question_times", 0)
    # 正常面试摘要
    interview_summary = state.get("interview_summary", "")
    # 对话历史
    messages = state.get("messages", [])
    recent_messages = messages[-times:]


    structured_llm = llm.with_structured_output(
        QuestionEvaluation
    )

    prompt = f"""
你是一名专业的技术面试官，请对用户本题的整个答题过程进行综合评价。

本次面试的摘要：
{interview_summary}

对话历史：
{recent_messages}


请结合面试摘要、最近的对话和用户本次回答，评价其对这道题的掌握程度。

评价字段说明：

1. score：综合得分，范围 0-100。
2. quality：回答质量。
   - GOOD：技术理解正确，表达清晰。
   - AVERAGE：基本理解，但存在不足。
   - WEAK：存在明显错误或理解不足。
3. completeness：回答完整性。
   - GOOD：覆盖主要知识点。
   - AVERAGE：覆盖部分关键知识点。
   - WEAK：遗漏大部分关键知识点。
4. correct_points：用户正确回答的知识点。
5. missing_points：用户遗漏的知识点。
6. error_points：用户回答中存在的错误。
7. feedback：对用户本题整体表现的综合评价。
8. next_difficulty：下一题的难度。

特别要求：
1. 必须综合所有回答，不能只看最后一次回答。
2. 如果用户在后续回答中纠正了之前的错误，应认可其改进。
3. 不要将 AI 提供的提示内容当作用户自己掌握的知识。
4. 不要编造用户没有表达过的知识点。
5. 如果用户最终仍然没有回答正确，应如实评价。
6. 使用中文。
7.下一题难度要根据用户的回答和评价结果来确定。
"""

    evaluation = structured_llm.invoke(prompt)
    current_difficulty = (
        evaluation.next_difficulty
        if state["difficulty_mode"] == "AUTO"
        else state["current_difficulty"]
    )

    return {
        "question_evaluation": evaluation,
        "current_difficulty": current_difficulty,
    }


def update_interview_summary(state: InterviewState) -> dict:
    """将刚结束的正式题整合进全场面试摘要。"""
    times = state.get("answer_times", 0) + state.get("question_times", 0)
    messages = state.get("messages", [])
    current_question_messages = messages[-times:] if times else []
    formal_question = (
        current_question_messages[0].content
        if current_question_messages
        else state.get("current_question", "")
    )

    prompt = f"""
你是一名专业的技术面试记录员。请将刚结束的正式题表现整合进整场面试摘要。

已有面试摘要：
{state.get("interview_summary", "")}

本题题目：
{formal_question}

本题对话（区分 Human 与 AI 消息）：
{current_question_messages}

本题最终评价：
{state.get("question_evaluation", "")}

要求：
1. 在保留已有摘要中仍然重要的信息的基础上，补充本题的新信息，输出更新后的完整摘要。
2. 总结用户实际表达出的知识点、掌握程度、错误或遗漏，以及回答过程中的改进。
3. AI 的提示、指导和追问不能算作用户掌握的知识；只把用户明确表达的内容归纳为其表现。
4. 不要编造对话或评价中没有的信息，语言简洁，使用中文。
只输出摘要正文。
"""

    response = llm.invoke(prompt)
    summary = response.content.strip()
    if not summary:
        raise ValueError("AI 未能生成有效的面试摘要")

    return {"interview_summary": summary}