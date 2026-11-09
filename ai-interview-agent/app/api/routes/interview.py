from fastapi import APIRouter, Request

from app.api.schemas.interview import (
    InterviewContinueRequest,
    InterviewContinueResponse,
    InterviewStartResponse,
    InterviewStartRequest,
)
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

#获取用户回答并返回询问或是追问
@router.post("/continue", response_model=InterviewContinueResponse)
async def continue_interview(
    data: InterviewContinueRequest,
    request:Request
):
    graph = request.app.state.interview_graph
    return InterviewService.continue_interview(
        data,
        graph
        )