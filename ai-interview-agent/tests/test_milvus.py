import sys
from pathlib import Path
from unittest import TestCase
from unittest.mock import patch

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from app.core.milvus import milvus_client
from app.core.config import settings
from scripts.init_milvus import init_milvus_collection


class InitMilvusCollectionTests(TestCase):
    @patch("scripts.init_milvus.MilvusClient")
    def test_uses_configured_uri_without_connecting_to_milvus(self, client_factory):
        client = client_factory.return_value
        client.has_collection.return_value = False
        client.describe_collection.return_value = {"fields": []}
        client.list_indexes.return_value = []

        init_milvus_collection()

        client_factory.assert_called_once_with(uri=settings.milvus_uri)
        client.create_collection.assert_called_once()
        client.close.assert_called_once()


def check_milvus():
    # 检查 Collection 是否存在
    exists = milvus_client.has_collection(
        collection_name=settings.milvus_collection_name
    )

    if not exists:
        raise RuntimeError("Milvus Collection 不存在，请检查配置")

    # 获取 Collection Schema
    schema = milvus_client.describe_collection(
        collection_name=settings.milvus_collection_name
    )

    print("Milvus 连接成功")
    print("Collection:", settings.milvus_collection_name)
    print("Schema:", schema)


if __name__ == "__main__":
    check_milvus()