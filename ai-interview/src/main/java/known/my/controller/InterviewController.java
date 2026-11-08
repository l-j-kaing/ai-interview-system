package known.my.controller;

import jakarta.validation.Valid;
import known.my.common.result.Result;
import known.my.pojo.dto.InterviewStartDTO;
import known.my.pojo.vo.InterviewStartVO;
import known.my.service.InterviewService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/interview")
@AllArgsConstructor
public class InterviewController {
    private final InterviewService interviewService;

    /**
     *开始面试
     */
    @PostMapping("/start")
    public Result<InterviewStartVO> startInterview(@RequestBody
                                                   @Valid InterviewStartDTO interviewStartDTO) {
        log.info("开始面试");
        InterviewStartVO interviewStartVO = interviewService.startInterview(interviewStartDTO);
        return Result.success(interviewStartVO);
    }
}
