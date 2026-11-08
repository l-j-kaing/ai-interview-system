package known.my.service;

import known.my.pojo.dto.InterviewStartDTO;
import known.my.pojo.vo.InterviewStartVO;

public interface InterviewService {
    InterviewStartVO startInterview(InterviewStartDTO interviewStartDTO);
}
