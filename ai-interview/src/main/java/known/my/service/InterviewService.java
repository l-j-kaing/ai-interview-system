package known.my.service;

import jakarta.validation.Valid;
import known.my.pojo.dto.InterviewContinueDTO;
import known.my.pojo.dto.InterviewStartDTO;
import known.my.pojo.vo.InterviewContinueVO;
import known.my.pojo.vo.InterviewStartVO;

public interface InterviewService {
    InterviewStartVO startInterview(InterviewStartDTO interviewStartDTO);

    InterviewContinueVO continueInterview(@Valid InterviewContinueDTO interviewContinueDTO);
}
