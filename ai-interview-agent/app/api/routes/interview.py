from fastapi import APIRouter, Request

from app.api.schemas.interview import InterviewStartResponse,InterviewStartRequest
from app.services.interview_service import InterviewService

router = APIRouter(
    prefix="/api/interview",
    tags=["Interview"]
)

#生成第一道面试题
@router.post("/start", response_model=InterviewStartResponse)
async def start_interview(
    data: InterviewStartRequest,
    request:Request
):
    #获取graph
    graph = request.app.state.interview_graph

    return InterviewService.start_interview(
        data,
        graph
        )