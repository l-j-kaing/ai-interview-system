
from langchain_core.documents import Document

from app.core.vector_store import vector_store


def add_pdf_chunks(
    chunks: list[Document],
    user_id: int,
    file_id: str,
    category: list[str],
    visibility: str,
    batch_size: int = 100,
) -> int:
    """
    将 PDF 分块写入 Milvus。

    每个 Chunk 保留所属 PDF 的页码，
    并补充业务所需的 metadata。
    """

    if not chunks:
        return 0

    documents = []

    for i in range(0,len(chunks)):
        text = chunks[i].page_content.strip()

        # 跳过空文本
        if not text:
            continue

        # Milvus text 字段 max_length = 10000
        # 使用 UTF-8 字节数检查，避免中文文本超长
        if len(text.encode("utf-8")) > 10000:
            raise ValueError(
                f"Chunk 超出 Milvus text 字段限制，"
                f"UTF-8 字节数：{len(text.encode('utf-8'))}"
            )

        # PyMuPDFLoader 的 page 通常从 0 开始
        page = chunks[i].metadata.get("page")

        if page is None:
            raise ValueError("PDF Chunk 缺少 page metadata")

        # 只保留 Collection 中存在的业务字段
        metadata = {
            "user_id": user_id,
            "file_id": file_id,
            "page_number": int(page) + 1,
            "category": category[i],
            "visibility": visibility,
        }

        documents.append(
            Document(
                page_content=text,
                metadata=metadata,
            )
        )

    # 分批写入，避免一次性提交过多数据
    inserted_count = 0

    for start in range(0, len(documents), batch_size):
        batch = documents[start:start + batch_size]

        vector_store.add_documents(batch)

        inserted_count += len(batch)

    return inserted_count
