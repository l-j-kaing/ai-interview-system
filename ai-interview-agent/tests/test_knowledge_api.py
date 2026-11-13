import unittest
from pathlib import Path
from unittest.mock import patch

from fastapi import FastAPI
from fastapi.testclient import TestClient
from langchain_core.documents import Document

from app.api.routes import knowledge_router


class MemoryRedis:
    def __init__(self):
        self.values = {}

    async def set(self, key, value, ex=None):
        self.values[key] = value

    async def get(self, key):
        return self.values.get(key)

    async def delete(self, key):
        self.values.pop(key, None)


class KnowledgeApiTests(unittest.TestCase):
    def setUp(self):
        app = FastAPI()
        app.include_router(knowledge_router)
        app.state.redis = MemoryRedis()
        self.client = TestClient(app)
        self.patches = [
            patch(
                "app.services.knowledge_service.resolve_path",
                return_value=Path("test.pdf"),
            ),
            patch(
                "app.services.knowledge_service.PyMuPDFLoader",
                return_value=type(
                    "FakeLoader",
                    (),
                    {
                        "load": lambda self: [
                            Document(
                                page_content=(
                                    "Knowledge integration test sentence. " * 12
                                ),
                                metadata={"page": 0},
                            )
                        ]
                    },
                )(),
            ),
            patch(
                "app.services.knowledge_service.add_pdf_chunks",
                side_effect=lambda chunks, **kwargs: len(chunks),
            ),
        ]
        self.resolve_path, self.loader, self.add_chunks = [
            item.start() for item in self.patches
        ]

    def tearDown(self):
        for item in reversed(self.patches):
            item.stop()
        self.client.close()

    def test_java_chunk_and_vectorize_payloads_round_trip(self):
        chunk_response = self.client.post(
            "/api/knowledge/chunk/punctuation",
            json={
                "file_path": "13/test-file.pdf",
                "chunk_size": 100,
                "chunk_overlap": 10,
                "split_punctuations": ["."],
                "file_id": "test-file",
                "user_id": 13,
            },
        )

        self.assertEqual(chunk_response.status_code, 200)
        chunks = chunk_response.json()["chunks"]
        self.assertGreater(len(chunks), 1)
        self.resolve_path.assert_called_once_with("13/test-file.pdf")

        vectorize_response = self.client.post(
            "/api/knowledge/vectorize",
            json={
                "file_id": "test-file",
                "chunks": chunks,
                "category": ["integration-test"],
                "user_id": 13,
                "visibility": "private",
            },
        )

        self.assertEqual(vectorize_response.status_code, 200)
        self.assertEqual(vectorize_response.json(), None)
        self.add_chunks.assert_called_once()
        ingested_chunks = self.add_chunks.call_args.kwargs
        self.assertEqual(ingested_chunks["user_id"], 13)
        self.assertEqual(ingested_chunks["file_id"], "test-file")
        self.assertEqual(ingested_chunks["visibility"], "private")
        self.assertEqual(
            ingested_chunks["category"],
            ["integration-test"] * len(chunks),
        )
        self.assertFalse(self.client.app.state.redis.values)


if __name__ == "__main__":
    unittest.main()
