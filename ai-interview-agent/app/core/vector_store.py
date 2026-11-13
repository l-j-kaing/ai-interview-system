from langchain_milvus import BM25BuiltInFunction, Milvus

from app.core.config import settings
from app.core.embedding import embedding_model


vector_store = Milvus(
    embedding_function=embedding_model,

    collection_name=settings.milvus_collection_name,

    connection_args={
        "uri": settings.milvus_uri
    },

    # 指定主键字段
    primary_field="id",

    # 指定文本字段
    text_field="text",

    # Dense 向量由 BGE-M3 生成
    # Sparse 向量由 Milvus 的 BM25 Function 生成
    vector_field=["dense_vector", "sparse"],

    # 关联已有 BM25 Function
    builtin_function=BM25BuiltInFunction(
        input_field_names="text",
        output_field_names="sparse",
    ),

    # 使用已有字段，不允许动态字段
    enable_dynamic_field=False,

    # 禁止删除已有 Collection
    drop_old=False,
)