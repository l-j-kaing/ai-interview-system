from langchain_text_splitters import RecursiveCharacterTextSplitter
from redis import Redis

from app.api.schemas.knowledge import ChunkByPunctuationRequest, VectorizeRequest
from app.utils.pending_documnet_store import delete_pending_documents, get_pending_documents, save_pending_documents
from app.utils.resolve_Path import resolve_path
from langchain_community.document_loaders import PyMuPDFLoader

from app.utils.vector_ingestion import add_pdf_chunks

class KnowledgeService:
    @staticmethod
    async def chunkByPunctuation(data:ChunkByPunctuationRequest,redis:Redis)-> list[str]:
        #读取文件
        file_absolute_path = resolve_path(data.file_path)
        #1.加载文件
        loader = PyMuPDFLoader(file_absolute_path)
        documents = loader.load()

        
         # 2. 构造分隔符
        separators = data.split_punctuations + ["\n\n", "\n", ""]
        
         # 3. 创建文本分块器
        text_splitter = RecursiveCharacterTextSplitter(
             separators=separators,
             chunk_size=data.chunk_size,
             chunk_overlap=data.chunk_overlap,
             length_function=len,
             keep_separator=True,
             is_separator_regex=False,
         )
        
         # 4. 执行分块
        chunks = text_splitter.split_documents(documents)
        #5.将分块结果进行存储
        await save_pending_documents(
            redis=redis,
            user_id=data.user_id,
            file_id=data.file_id,
            documents=chunks
        )
        chunks = [chunk.page_content for chunk in chunks]

        return chunks

    @staticmethod
    async def vectorize(data:VectorizeRequest,redis:Redis)-> None:
        """
        向量化文件
        """
        chunks = await get_pending_documents(
            redis=redis,
            user_id=data.user_id,
            file_id=data.file_id
        )
        if chunks is None:
            raise ValueError("缓存不存在或已过期")
        #进行向量化
        categories = data.category
        if len(categories) == 1:
            categories = categories * len(chunks)
        elif len(categories) != len(chunks):
            raise ValueError("分类数量必须为 1 或与分块数量一致")

        inserted_count = add_pdf_chunks(
            chunks=chunks,
            user_id=data.user_id,
            file_id=data.file_id,
            category=categories,
            visibility=data.visibility
        )
        if inserted_count == 0:
            raise ValueError("向量化失败")

        
        await delete_pending_documents(
            redis=redis,
            user_id=data.user_id,
            file_id=data.file_id
        )
