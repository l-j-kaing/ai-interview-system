from typing import Annotated, NotRequired, TypedDict

from langchain_core.messages import AnyMessage
from langgraph.graph.message import add_messages


class InterviewState(TypedDict):
  
  # 一、面试基本信息
    interview_id: str
    job_position: str
    tech_direction: str
    difficulty_mode: str
    current_difficulty: str

    # 二、当前正式题
    question_no: int
    current_question: NotRequired[str]

    # 三、当前轮用户输入
    current_answer: NotRequired[str]

    # 四、当前面试次数统计
    question_times:int
    answer_times: int

    # 五、全场短期对话历史
    messages: Annotated[list[AnyMessage], add_messages]

    # 六、整场面试摘要
    interview_summary: str

    # 七、流程选择
    next_action: NotRequired[str]
    

    # 八、当前题最终评价
    question_evaluation: NotRequired[dict]


    