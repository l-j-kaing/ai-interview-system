from langgraph.checkpoint.redis import RedisSaver

from app.core.config import settings

ttl_config = {
    "default_ttl": settings.redis_checkpoint_ttl,
    "refresh_on_read": True,
}

def create_checkpointer():
    #Redis的持久化Checkpointer
    checkpointer = RedisSaver.from_conn_string(
        settings.redis_url,
        ttl = ttl_config
    )

    return checkpointer