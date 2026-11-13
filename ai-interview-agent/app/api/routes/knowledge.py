from fastapi import APIRouter, Request
from app.api.schemas.knowledge import ChunkByPunctuationRequest, VectorizeRequest
from app.services.knowledge_service import KnowledgeService

router = APIRouter(
    prefix="/api/knowledge",
    tags=["Knowledge"]
)

#进行分块
@router.post("/chunk/punctuation")
async def chunk_by_Punctuation(data: ChunkByPunctuationRequest,request:Request)->list[str]:
    redis = request.app.state.redis
    return KnowledgeService.chunkByPunctuation(data,redis)

#进行向量化
@router.post("/vectorize")
async def vectorize(data: VectorizeRequest,request:Request)->None:
    redis = request.app.state.redis
    return KnowledgeService.vectorize(data,redis)