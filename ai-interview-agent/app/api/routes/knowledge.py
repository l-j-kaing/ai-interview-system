from fastapi import APIRouter, Request
from app.api.schemas.knowledge import (
    ChunkByPunctuationRequest,
    ChunkByPunctuationResponse,
    VectorizeRequest,
)
from app.services.knowledge_service import KnowledgeService

router = APIRouter(
    prefix="/api/knowledge",
    tags=["Knowledge"]
)

#进行分块
@router.post("/chunk/punctuation", response_model=ChunkByPunctuationResponse)
async def chunk_by_punctuation(
    data: ChunkByPunctuationRequest,
    request: Request,
) -> ChunkByPunctuationResponse:
    redis = request.app.state.redis
    chunks = await KnowledgeService.chunkByPunctuation(data, redis)
    return ChunkByPunctuationResponse(chunks=chunks)

#进行向量化
@router.post("/vectorize")
async def vectorize(data: VectorizeRequest,request:Request)->None:
    redis = request.app.state.redis
    await KnowledgeService.vectorize(data, redis)