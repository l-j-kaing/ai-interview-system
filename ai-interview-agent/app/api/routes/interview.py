from fastapi import APIRouter, HTTPException, status

from app.api.schemas.interview import InterviewStartRequest

router = APIRouter(prefix="/interview", tags=["interview"])


@router.post("/start", status_code=status.HTTP_501_NOT_IMPLEMENTED)
async def start_interview(request: InterviewStartRequest) -> None:
    """面试启动工作流的占位接口。"""
    raise HTTPException(
        status_code=status.HTTP_501_NOT_IMPLEMENTED,
        detail="面试工作流尚未实现。",
    )
