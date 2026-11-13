import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from pymilvus import MilvusClient, DataType, Function, FunctionType

from app.core.config import settings


def init_milvus_collection():
    collection_name = settings.milvus_collection_name

    client = MilvusClient(
        uri=settings.milvus_uri
    )

    try:
        print("Milvus 连接成功")
        print(f"Collection: {collection_name}")

        # 1. 删除旧 Collection
        if client.has_collection(collection_name):
            print("发现旧 Collection，正在删除...")
            client.drop_collection(collection_name)
            print("旧 Collection 删除成功")

        # 2. 创建 Schema
        schema = client.create_schema(
            auto_id=True,
            enable_dynamic_field=False,
        )

        # 主键
        schema.add_field(
            field_name="id",
            datatype=DataType.INT64,
            is_primary=True,
        )

        # 用户 ID
        schema.add_field(
            field_name="user_id",
            datatype=DataType.INT64,
        )

        # 文件 ID
        schema.add_field(
            field_name="file_id",
            datatype=DataType.VARCHAR,
            max_length=100,
        )

        # PDF 页码，允许为空
        schema.add_field(
            field_name="page_number",
            datatype=DataType.INT32,
            nullable=True,
        )

        # 原始文本
        schema.add_field(
            field_name="text",
            datatype=DataType.VARCHAR,
            max_length=10000,
            enable_analyzer=True,
        )

        # Dense Vector，1024 维
        schema.add_field(
            field_name="dense_vector",
            datatype=DataType.FLOAT_VECTOR,
            dim=1024,
        )

        # Sparse Vector
        schema.add_field(
            field_name="sparse",
            datatype=DataType.SPARSE_FLOAT_VECTOR,
        )

        # 分类
        schema.add_field(
            field_name="category",
            datatype=DataType.VARCHAR,
            max_length=100,
        )

        # 可见性
        schema.add_field(
            field_name="visibility",
            datatype=DataType.VARCHAR,
            max_length=20,
        )

        # 3. 配置 BM25 Function
        bm25_function = Function(
            name="text_bm25_emb",
            input_field_names=["text"],
            output_field_names=["sparse"],
            function_type=FunctionType.BM25,
        )

        schema.add_function(bm25_function)

        # 4. 创建索引参数
        index_params = client.prepare_index_params()

        # Dense Vector 索引
        index_params.add_index(
            field_name="dense_vector",
            index_type="AUTOINDEX",
            metric_type="COSINE",
        )

        # Sparse Vector / BM25 索引
        index_params.add_index(
            field_name="sparse",
            index_type="SPARSE_INVERTED_INDEX",
            metric_type="BM25",
            params={
                "inverted_index_algo": "DAAT_MAXSCORE",
                "bm25_k1": 1.2,
                "bm25_b": 0.75,
            },
        )

        # 5. 创建 Collection
        client.create_collection(
            collection_name=collection_name,
            schema=schema,
            index_params=index_params,
        )

        print("\nCollection 创建成功！")

        # 6. 查询并验证 Schema
        collection_info = client.describe_collection(
            collection_name=collection_name
        )

        print("\n========== Collection Schema ==========")

        for field in collection_info["fields"]:
            print(
                f"字段名称: {field['name']}, "
                f"字段类型: {field['type']}"
            )

        # 7. 验证索引
        print("\n========== Collection Indexes ==========")

        indexes = client.list_indexes(
            collection_name=collection_name
        )

        for index in indexes:
            print(index)

        print("\nMilvus 初始化完成！")

    finally:
        client.close()


if __name__ == "__main__":
    init_milvus_collection()
