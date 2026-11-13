from pydantic import BaseModel, Field


class ChunkByPunctuationRequest(BaseModel):
    file_path: str=Field(...,description="文件路径")
    chunk_size: int=Field(...,description="分块大小")
    chunk_overlap: int=Field(...,description="分块重叠大小")
    split_punctuations: list[str]=Field(...,description="分块 punctuation")
    file_id: str=Field(...,description="文件 ID")
    user_id: int=Field(...,description="用户 ID")


class ChunkByPunctuationResponse(BaseModel):
    chunks: list[str]=Field(...,description="分块结果")



class VectorizeRequest(BaseModel):
    file_id: str=Field(...,description="文件 ID")
    chunks: list[str]=Field(...,description="分块结果")
    category: list[str]=Field(...,description="分类")
    user_id: int=Field(...,description="用户 ID")
    visibility: str=Field(...,description="可见性")