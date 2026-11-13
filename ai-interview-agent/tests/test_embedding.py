import sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))


import asyncio

from app.core.embedding import embedding_model


async def main():
    texts = [
        "HashMap 是基于哈希表实现的 Map 接口",
        "ArrayList 底层使用动态数组实现",
    ]

    vectors = await embedding_model.aembed_documents(texts)

    print("文本数量：", len(vectors))
    print("向量维度：", len(vectors[0]))
    print("第一个向量前 5 个值：", vectors[0][:5])


if __name__ == "__main__":
    asyncio.run(main())