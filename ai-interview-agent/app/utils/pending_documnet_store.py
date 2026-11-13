import pickle
from redis.asyncio import Redis
from langchain_core.documents import Document


# 待确认分块数据的过期时间：30 分钟
PENDING_DOCUMENT_TTL = 1800


def get_pending_document_key(user_id: int, file_id: str) -> str:
    """
    根据用户 ID 和文件 ID 生成 Redis Key。
    """
    return f"knowledge:pending:{user_id}:{file_id}"


async def save_pending_documents(
    redis: Redis,
    user_id: int,
    file_id: str,
    documents: list[Document],
) -> None:
    """
    将分块后的 Document 列表序列化并保存到 Redis。
    """
    redis_key = get_pending_document_key(user_id, file_id)

    # Python 对象序列化为 bytes
    data = pickle.dumps(
        documents,
        protocol=pickle.HIGHEST_PROTOCOL,
    )

    await redis.set(
        redis_key,
        data,
        ex=PENDING_DOCUMENT_TTL,
    )


async def get_pending_documents(
    redis: Redis,
    user_id: int,
    file_id: str,
) -> list[Document] | None:
    """
    根据用户 ID 和文件 ID 获取待确认的 Document 列表。
    如果缓存不存在或已过期，返回 None。
    """
    redis_key = get_pending_document_key(user_id, file_id)

    data = await redis.get(redis_key)

    if data is None:
        return None

    # bytes 反序列化为 Python 对象
    documents = pickle.loads(data)

    if not isinstance(documents, list):
        raise TypeError("Redis 中保存的数据不是 List")

    if not all(isinstance(doc, Document) for doc in documents):
        raise TypeError("Redis 中存在非 Document 类型的数据")

    return documents


async def delete_pending_documents(
    redis: Redis,
    user_id: int,
    file_id: str,
) -> None:
    """
    删除待确认的分块数据。
    """
    redis_key = get_pending_document_key(user_id, file_id)

    await redis.delete(redis_key)