import unittest
from types import SimpleNamespace

from fastapi import FastAPI
from fastapi.testclient import TestClient
from langchain_core.messages import AIMessage
from langgraph.graph.state import Command

from app.api.routes import interview_router


class FakeGraph:
    def get_state(self, config):
        return SimpleNamespace(
            values={"question_no": 1},
            tasks=[SimpleNamespace(interrupts=[object()])],
        )

    def invoke(self, graph_input, config):
        if isinstance(graph_input, Command):
            return {
                "__interrupt__": [object()],
                "question_no": 1,
                "current_question": "原始正式题",
                "current_difficulty": "MEDIUM",
                "messages": [
                    AIMessage(content="原始正式题"),
                    AIMessage(content="提示：请补充关键原理。"),
                ],
            }
        return {
            "interview_id": graph_input["interview_id"],
            "current_question": "原始正式题",
            "current_difficulty": graph_input["current_difficulty"],
            "question_no": 1,
        }


class InterviewApiTests(unittest.TestCase):
    def setUp(self):
        app = FastAPI()
        app.include_router(interview_router)
        app.state.interview_graph = FakeGraph()
        self.client = TestClient(app)

    def test_start_accepts_java_client_snake_case_payload(self):
        response = self.client.post(
            "/api/interview/start",
            json={
                "interview_id": "api-test",
                "job_position": "Java 后端工程师",
                "tech_direction": "Java",
                "difficulty_mode": "AUTO",
                "current_difficulty": "MEDIUM",
            },
        )

        self.assertEqual(response.status_code, 200)
        self.assertEqual(
            response.json(),
            {
                "question": "原始正式题",
                "difficulty": "MEDIUM",
                "question_no": 1,
                "interview_id": "api-test",
            },
        )

    def test_continue_returns_hint_in_java_client_response_shape(self):
        response = self.client.post(
            "/api/interview/continue",
            json={"interview_id": "api-test", "answer": "初步回答"},
        )

        self.assertEqual(response.status_code, 200)
        body = response.json()
        self.assertEqual(body["status"], "CONTINUE")
        self.assertEqual(body["question_no"], 1)
        self.assertEqual(body["question"], "提示：请补充关键原理。")


if __name__ == "__main__":
    unittest.main()
