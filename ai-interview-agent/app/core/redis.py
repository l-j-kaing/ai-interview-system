from redis.asyncio import Redis
from app.core.config import settings


# 创建 Redis 异步客户端
redis_client = Redis.from_url(
    settings.redis_url,
    decode_responses=False,
    encoding="utf-8",
)


# 检查 Redis 连接
async def check_redis_connection():
    try:
        await redis_client.ping()
        print("Redis 连接成功")
    except Exception as e:
        print(f"Redis 连接失败: {e}")
        raise


# 关闭 Redis 连接
async def close_redis_connection():
    await redis_client.aclose()