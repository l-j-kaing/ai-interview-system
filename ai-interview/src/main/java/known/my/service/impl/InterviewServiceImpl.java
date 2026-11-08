package known.my.service.impl;

import known.my.client.PythonAgentClient;
import known.my.common.context.BaseContext;
import known.my.mapper.InterviewMapper;
import known.my.pojo.dto.InterviewStartDTO;
import known.my.pojo.entity.Interview;
import known.my.pojo.enums.InterviewStatus;
import known.my.pojo.request.AgentInterviewStartRequest;
import known.my.pojo.response.AgentInterviewStartResponse;
import known.my.pojo.vo.InterviewStartVO;
import known.my.service.InterviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {
    private final InterviewMapper interviewMapper;
    private final InterviewStartPersistenceService interviewStartPersistenceService;
    private final PythonAgentClient pythonAgentClient;

    /** 创建面试记录，调用 Python Agent 生成首题并保存到数据库。 */
    @Override
    public InterviewStartVO startInterview(InterviewStartDTO interviewStartDTO) {
        // 先生成面试 ID 并持久化，后续将同一 ID 传给 Python Agent。
        Interview interview = Interview.builder()
                .interviewId(UUID.randomUUID().toString())
                .userId(BaseContext.getCurrentId())
                .jobPosition(interviewStartDTO.getJobPosition())
                .techDirection(interviewStartDTO.getTechDirection())
                .difficultyMode(interviewStartDTO.getDifficultyMode())
                .status(InterviewStatus.CREATED.name())
                .questionCount(0)
                .startedAt(LocalDateTime.now())
                .build();
        interviewMapper.insertInterview(interview);

        try {
            AgentInterviewStartRequest agentRequest = AgentInterviewStartRequest.builder()
                    .interviewId(interview.getInterviewId())
                    .jobPosition(interviewStartDTO.getJobPosition())
                    .techDirection(interviewStartDTO.getTechDirection())
                    .difficultyMode(interviewStartDTO.getDifficultyMode())
                    .currentDifficulty(interviewStartDTO.getCurrentDifficulty())
                    .build();
            AgentInterviewStartResponse agentResponse = pythonAgentClient.startInterview(agentRequest);
            if (agentResponse == null || agentResponse.getQuestion() == null
                    || agentResponse.getQuestion().isBlank()
                    || agentResponse.getQuestionNo() == null || agentResponse.getQuestionNo() < 1
                    || agentResponse.getDifficulty() == null || agentResponse.getInterviewId() == null
                    || !interview.getInterviewId().equals(agentResponse.getInterviewId())) {
                throw new IllegalStateException("Python Agent 未返回有效的首道面试题");
            }

            interviewStartPersistenceService.saveFirstQuestion(interview, agentResponse);
            return InterviewStartVO.builder()
                    .interviewId(interview.getInterviewId())
                    .questionNo(agentResponse.getQuestionNo())
                    .content(agentResponse.getQuestion())
                    .build();
        } catch (Exception e) {
            interviewMapper.updateInterviewStatus(
                    interview.getInterviewId(),
                    InterviewStatus.ABORTED.name(),
                    null,
                    null
            );
            log.error("启动面试失败，interviewId={}", interview.getInterviewId(), e);
            throw new IllegalStateException("启动面试失败", e);
        }
    }

}
