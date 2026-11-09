import unittest
from types import SimpleNamespace
from unittest.mock import patch

from langgraph.checkpoint.memory import InMemorySaver

from app.agents.interview.graph import build_interview_graph
from app.agents.interview.schemas import (
    AnswerDecision,
    AnswerProcessEvaluation,
    EvaluationLevel,
    QuestionEvaluation,
    QuestionDifficulty,
)
from app.api.schemas.interview import (
    Difficulty,
    DifficultyMode,
    InterruptStatus,
    InterviewContinueRequest,
    InterviewStartRequest,
)
from app.services.interview_service import InterviewService


class FakeLLM:
    def __init__(self):
        self.decisions = iter(
            [
                AnswerDecision.RETRY_WITH_HINT,
                AnswerDecision.FOLLOW_UP,
                AnswerDecision.NEXT_QUESTION,
                AnswerDecision.FINISHED,
            ]
        )
        self.question_number = 0

    def invoke(self, prompt):
        if "请根据以下信息生成一道技术面试题" in prompt:
            self.question_number += 1
            return SimpleNamespace(content=f"正式面试题 {self.question_number}")
        if "请给用户一个适当的提示" in prompt:
            return SimpleNamespace(content="提示：请补充关键原理。")
        if "生成一道有针对性的深入追问" in prompt:
            return SimpleNamespace(content="追问：这个机制在并发场景下如何工作？")
        if "技术面试记录员" in prompt:
            return SimpleNamespace(content="用户能够逐步完善技术回答。")
        raise AssertionError(f"Unexpected LLM prompt: {prompt}")

    def with_structured_output(self, schema, *, method):
        if method != "function_calling":
            raise AssertionError(f"Unexpected structured output method: {method}")

        class StructuredLLM:
            def invoke(inner_self, prompt):
                if schema is AnswerProcessEvaluation:
                    return AnswerProcessEvaluation(decision=next(self.decisions))
                if schema is QuestionEvaluation:
                    return QuestionEvaluation(
                        score=85,
                        quality=EvaluationLevel.GOOD,
                        completeness=EvaluationLevel.AVERAGE,
                        correct_points=["解释了核心原理"],
                        missing_points=["未讨论边界情况"],
                        error_points=[],
                        feedback="回答逻辑清晰。",
                        next_difficulty=QuestionDifficulty.EASY,
                    )
                raise AssertionError(f"Unexpected output schema: {schema}")

        return StructuredLLM()


class InterviewFlowTests(unittest.TestCase):
    def test_hint_follow_up_next_question_and_finish(self):
        llm = FakeLLM()
        graph = build_interview_graph(InMemorySaver())

        with patch("app.agents.interview.nodes.llm", llm):
            started = InterviewService.start_interview(
                InterviewStartRequest(
                    interview_id="flow-test",
                    job_position="Java 后端工程师",
                    tech_direction="Java",
                    difficulty_mode=DifficultyMode.AUTO,
                    current_difficulty=Difficulty.MEDIUM,
                ),
                graph,
            )
            self.assertEqual(started.question_no, 1)
            self.assertEqual(started.question, "正式面试题 1")

            first_reply = InterviewService.continue_interview(
                InterviewContinueRequest(
                    interview_id="flow-test",
                    answer="HashMap 是一个集合。",
                ),
                graph,
            )
            self.assertEqual(first_reply.status, InterruptStatus.CONTINUE)
            self.assertEqual(first_reply.question, "提示：请补充关键原理。")
            self.assertNotEqual(first_reply.question, started.question)

            second_reply = InterviewService.continue_interview(
                InterviewContinueRequest(
                    interview_id="flow-test",
                    answer="它通过哈希定位桶，冲突时使用链表或树。",
                ),
                graph,
            )
            self.assertEqual(second_reply.status, InterruptStatus.CONTINUE)
            self.assertEqual(
                second_reply.question,
                "追问：这个机制在并发场景下如何工作？",
            )

            next_question = InterviewService.continue_interview(
                InterviewContinueRequest(
                    interview_id="flow-test",
                    answer="通过链表或红黑树处理冲突。",
                ),
                graph,
            )
            self.assertEqual(next_question.status, InterruptStatus.NEXT_QUESTION)
            self.assertEqual(next_question.completed_question_no, 1)
            self.assertEqual(next_question.question_no, 2)
            self.assertEqual(next_question.question, "正式面试题 2")
            self.assertEqual(next_question.difficulty, Difficulty.EASY)
            self.assertEqual(next_question.question_evaluation["score"], 85)

            finished = InterviewService.continue_interview(
                InterviewContinueRequest(
                    interview_id="flow-test",
                    answer="我已完成第二题。",
                ),
                graph,
            )
            self.assertEqual(finished.status, InterruptStatus.FINISHED)
            self.assertEqual(finished.question_no, 2)
            self.assertEqual(finished.question_evaluation["score"], 85)


if __name__ == "__main__":
    unittest.main()
