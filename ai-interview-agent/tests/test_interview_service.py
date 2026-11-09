import unittest
from types import SimpleNamespace

from langchain_core.messages import AIMessage

from app.api.schemas.interview import (
    InterruptStatus,
    InterviewContinueRequest,
)
from app.services.interview_service import InterviewService


class ContinueInterviewTests(unittest.TestCase):
    def test_retry_returns_latest_assistant_hint_instead_of_original_question(self):
        original_question = "请解释 Java 中 HashMap 的工作原理。"
        hint = "可以从哈希计算、桶定位和冲突处理几个方面回答。"

        class FakeGraph:
            def get_state(self, config):
                return SimpleNamespace(
                    values={"question_no": 1},
                    tasks=[SimpleNamespace(interrupts=[object()])],
                )

            def invoke(self, command, config):
                return {
                    "__interrupt__": [object()],
                    "question_no": 1,
                    "current_question": original_question,
                    "current_difficulty": "MEDIUM",
                    "messages": [
                        AIMessage(content=original_question),
                        AIMessage(content=hint),
                    ],
                }

        response = InterviewService.continue_interview(
            InterviewContinueRequest(
                interview_id="interview-1",
                answer="HashMap 是一种集合。",
            ),
            FakeGraph(),
        )

        self.assertEqual(response.status, InterruptStatus.CONTINUE)
        self.assertEqual(response.question, hint)
        self.assertNotEqual(response.question, original_question)
