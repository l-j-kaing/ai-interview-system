package known.my.service.impl;

import known.my.client.PythonAgentClient;
import known.my.common.context.BaseContext;
import known.my.mapper.InterviewMapper;
import known.my.mapper.InterviewMessageMapper;
import known.my.mapper.InterviewQuestionMapper;
import known.my.pojo.dto.InterviewContinueDTO;
import known.my.pojo.dto.InterviewStartDTO;
import known.my.pojo.entity.Interview;
import known.my.pojo.entity.InterviewMessage;
import known.my.pojo.entity.InterviewQuestion;
import known.my.pojo.enums.InterruptStatus;
import known.my.pojo.enums.InterviewStatus;
import known.my.pojo.enums.Role;
import known.my.pojo.request.AgentInterviewContinueRequest;
import known.my.pojo.request.AgentInterviewStartRequest;
import known.my.pojo.response.AgentInterviewContinueResponse;
import known.my.pojo.response.AgentInterviewStartResponse;
import known.my.pojo.vo.InterviewContinueVO;
import known.my.pojo.vo.InterviewStartVO;
import known.my.service.InterviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {
    private final InterviewMapper interviewMapper;
    private final PythonAgentClient pythonAgentClient;
    private final InterviewQuestionMapper interviewQuestionMapper;
    private final InterviewMessageMapper interviewMessageMapper;
    private final InterviewPersistenceService interviewPersistenceService;


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
                .build();
        if (interviewMapper.insertInterview(interview) != 1) {
            throw new IllegalStateException("未能创建面试记录");
        }

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
                    || agentResponse.getQuestionNo() == null || agentResponse.getQuestionNo() != 1
                    || agentResponse.getDifficulty() == null || agentResponse.getInterviewId() == null
                    || !interview.getInterviewId().equals(agentResponse.getInterviewId())) {
                throw new IllegalStateException("Python Agent 未返回有效的首道面试题");
            }

            interviewPersistenceService.saveFirstQuestion(
                    interview.getInterviewId(),
                    interview.getUserId(),
                    agentResponse.getQuestionNo(),
                    agentResponse.getQuestion(),
                    agentResponse.getDifficulty()
            );
            return InterviewStartVO.builder()
                    .interviewId(interview.getInterviewId())
                    .questionNo(agentResponse.getQuestionNo())
                    .content(agentResponse.getQuestion())
                    .build();
        } catch (Exception e) {
            interviewMapper.updateInterviewFieldsByInterviewId(
                    interview.getInterviewId(),
                    InterviewStatus.ABORTED.name(),
                    null,
                    null,
                    null
            );
            log.error("启动面试失败，interviewId={}", interview.getInterviewId(), e);
            throw new IllegalStateException("启动面试失败", e);
        }
    }
    /** 继续面试，传递用户答案，并获取下一道面试题或结束面试 */
    @Override
    public InterviewContinueVO continueInterview(InterviewContinueDTO interviewContinueDTO) {
        String interviewId = interviewContinueDTO.getInterviewId();
        Long userId = BaseContext.getCurrentId();
        Interview interview = interviewMapper.selectInterviewById(interviewId);
        if (interview == null || userId == null || !userId.equals(interview.getUserId())) {
            throw new IllegalStateException("面试不存在或当前用户无权访问");
        }
        if (!InterviewStatus.IN_PROGRESS.name().equals(interview.getStatus())) {
            throw new IllegalStateException("当前面试不处于进行中状态，无法继续回答");
        }

        InterviewQuestion interviewQuestion = interviewQuestionMapper.selectLatestQuestionByInterviewId(interviewId);
        if (interviewQuestion == null) {
            throw new IllegalStateException("未找到面试当前题目，interviewId=" + interviewId);
        }

        InterviewMessage interviewMessage = InterviewMessage.builder()
                .interviewId(interviewId)
                .userId(userId)
                .questionNo(interviewQuestion.getQuestionNo())
                .role(Role.USER.name())
                .content(interviewContinueDTO.getAnswer())
                .build();
        if (interviewMessageMapper.insertInterviewMessage(interviewMessage) != 1) {
            throw new IllegalStateException("未能保存用户回答历史，interviewId="
                    + interviewContinueDTO.getInterviewId());
        }

        AgentInterviewContinueRequest agentRequest = AgentInterviewContinueRequest.builder()
                .interviewId(interviewId)
                .answer(interviewContinueDTO.getAnswer())
                .build();
        AgentInterviewContinueResponse agentResponse = pythonAgentClient.continueInterview(
                agentRequest
        );
        if (agentResponse == null || agentResponse.getStatus() == null) {
            throw new IllegalStateException("Python Agent 未返回有效的面试状态");
        }

        return switch (agentResponse.getStatus()) {
            case "CONTINUE" -> saveFollowUp(interviewContinueDTO, userId, interviewQuestion, agentResponse);
            case "NEXT_QUESTION" -> saveNextQuestion(
                    interviewContinueDTO, userId, interviewQuestion, agentResponse);
            case "FINISHED" -> finishInterview(
                    interviewContinueDTO, userId, interviewQuestion, agentResponse);
            default -> throw new IllegalStateException("Python Agent 返回未知面试状态："
                    + agentResponse.getStatus());
        };
    }

    /** 追问仍属于当前正式题，只新增双方对话历史。 */
    private InterviewContinueVO saveFollowUp(InterviewContinueDTO request, Long userId,
                                             InterviewQuestion currentQuestion,
                                             AgentInterviewContinueResponse response) {
        if (response.getQuestion() == null || response.getQuestion().isBlank()) {
            throw new IllegalStateException("Python Agent 未返回当前追问内容");
        }
        if (response.getQuestionNo() != null
                && !response.getQuestionNo().equals(currentQuestion.getQuestionNo())) {
            throw new IllegalStateException("Python Agent 返回的追问题号与当前题目不一致");
        }
        interviewPersistenceService.saveAssistantMessage(
                request.getInterviewId(), userId, currentQuestion.getQuestionNo(), response.getQuestion());
        return InterviewContinueVO.builder()
                .question(response.getQuestion())
                .questionNo(currentQuestion.getQuestionNo())
                .status(InterruptStatus.CONTINUE.name())
                .build();
    }

    /** 完成当前题并原子地保存评分、下一题及其助手消息。 */
    private InterviewContinueVO saveNextQuestion(InterviewContinueDTO request, Long userId,
                                                 InterviewQuestion currentQuestion,
                                                 AgentInterviewContinueResponse response) {
        if (response.getQuestion() == null || response.getQuestion().isBlank()
                || response.getDifficulty() == null || response.getQuestionNo() == null
                || response.getQuestionEvaluation() == null
                || response.getCompletedQuestionNo() == null) {
            throw new IllegalStateException("Python Agent 未返回完整的已完成题目评价或下一题信息");
        }
        if (!response.getCompletedQuestionNo().equals(currentQuestion.getQuestionNo())
                || response.getQuestionNo() != currentQuestion.getQuestionNo() + 1) {
            throw new IllegalStateException("Python Agent 返回的完成题号或下一题号与当前面试进度不一致");
        }
        interviewPersistenceService.completeQuestionAndSaveNext(
                request.getInterviewId(),
                userId,
                response.getCompletedQuestionNo(),
                response.getQuestionEvaluation(),
                response.getQuestionNo(),
                response.getQuestion(),
                response.getDifficulty()
        );
        return InterviewContinueVO.builder()
                .question(response.getQuestion())
                .questionNo(response.getQuestionNo())
                .status(InterruptStatus.NEXT_QUESTION.name())
                .completedQuestionNo(response.getCompletedQuestionNo())
                .questionEvaluation(response.getQuestionEvaluation())
                .build();
    }

    /** 保存最后一题的评分、总评历史，并更新面试完成状态。 */
    private InterviewContinueVO finishInterview(InterviewContinueDTO request, Long userId,
                                                InterviewQuestion currentQuestion,
                                                AgentInterviewContinueResponse response) {
        Integer completedQuestionNo = response.getCompletedQuestionNo() != null
                ? response.getCompletedQuestionNo()
                : response.getQuestionNo();
        if (completedQuestionNo == null || response.getQuestionEvaluation() == null
                || !completedQuestionNo.equals(currentQuestion.getQuestionNo())) {
            throw new IllegalStateException("Python Agent 未返回与当前题目匹配的完整最终评价");
        }
        interviewPersistenceService.completeQuestionAndInterview(
                request.getInterviewId(),
                userId,
                completedQuestionNo,
                response.getQuestionEvaluation()
        );
        return InterviewContinueVO.builder()
                .questionNo(completedQuestionNo)
                .status(InterruptStatus.FINISHED.name())
                .completedQuestionNo(completedQuestionNo)
                .questionEvaluation(response.getQuestionEvaluation())
                .build();
    }

}
