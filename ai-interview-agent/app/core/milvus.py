from pymilvus import MilvusClient

from app.core.config import settings


milvus_client = MilvusClient(
    uri=settings.milvus_uri
)