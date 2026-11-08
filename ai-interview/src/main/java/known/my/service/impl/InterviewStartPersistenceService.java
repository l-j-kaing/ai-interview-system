package known.my.service.impl;

import known.my.mapper.InterviewMapper;
import known.my.mapper.InterviewMessageMapper;
import known.my.mapper.InterviewQuestionMapper;
import known.my.pojo.entity.Interview;
import known.my.pojo.entity.InterviewMessage;
import known.my.pojo.entity.InterviewQuestion;
import known.my.pojo.enums.InterviewStatus;
import known.my.pojo.enums.Role;
import known.my.pojo.response.AgentInterviewStartResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InterviewStartPersistenceService {
    private final InterviewMapper interviewMapper;
    private final InterviewQuestionMapper questionMapper;
    private final InterviewMessageMapper messageMapper;

    /** 持久化首题、消息和面试状态；任一步失败时整体回滚。 */
    @Transactional
    public void saveFirstQuestion(Interview interview, AgentInterviewStartResponse agentResponse) {
        InterviewQuestion question = InterviewQuestion.builder()
                .interviewId(interview.getInterviewId())
                .userId(interview.getUserId())
                .questionNo(agentResponse.getQuestionNo())
                .questionContent(agentResponse.getQuestion())
                .difficulty(agentResponse.getDifficulty())
                .answerCount(0)
                .followUpCount(0)
                .status(InterviewStatus.IN_PROGRESS.name())
                .build();
        questionMapper.insertInterviewQuestion(question);

        InterviewMessage message = InterviewMessage.builder()
                .interviewId(interview.getInterviewId())
                .userId(interview.getUserId())
                .questionNo(agentResponse.getQuestionNo())
                .role(Role.ASSISTANT.name())
                .content(agentResponse.getQuestion())
                .build();
        messageMapper.insertInterviewMessage(message);

        interviewMapper.updateInterviewStatus(
                interview.getInterviewId(),
                InterviewStatus.IN_PROGRESS.name(),
                1,
                null
        );
    }
}
