import unittest

from fastapi.testclient import TestClient

from app.main import app


class InterviewApiTests(unittest.TestCase):
    def setUp(self) -> None:
        self.client = TestClient(app)

    def test_health_check(self) -> None:
        response = self.client.get("/health")

        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json(), {"status": "ok"})

    def test_start_interview_is_a_placeholder(self) -> None:
        response = self.client.post(
            "/api/interview/start",
            json={
                "job_position": "后端开发工程师",
                "tech_direction": "Python",
                "difficulty_mode": "MEDIUM",
            },
        )

        self.assertEqual(response.status_code, 501)


if __name__ == "__main__":
    unittest.main()
