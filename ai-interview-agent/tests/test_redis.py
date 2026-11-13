import sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

import asyncio

from app.core.redis import (
    check_redis_connection,
    close_redis_connection,
)


async def main():
    await check_redis_connection()
    await close_redis_connection()


if __name__ == "__main__":
    asyncio.run(main())