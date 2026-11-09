package known.my.service.impl;

import known.my.mapper.InterviewMapper;
import known.my.mapper.InterviewMessageMapper;
import known.my.mapper.InterviewQuestionMapper;
import known.my.pojo.entity.Interview;
import known.my.pojo.entity.InterviewMessage;
import known.my.pojo.entity.InterviewQuestion;
import known.my.pojo.enums.InterviewQuestionStatus;
import known.my.pojo.enums.InterviewStatus;
import known.my.pojo.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InterviewPersistenceService {
    private final InterviewMapper interviewMapper;
    private final InterviewQuestionMapper questionMapper;
    private final InterviewMessageMapper messageMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 保存首题、助手消息和面试开始状态；任一步失败时整体回滚。 */
    @Transactional
    public void saveFirstQuestion(String interviewId, Long userId, Integer questionNo,
                                  String questionContent, String difficulty) {
        insertQuestionAndAssistantMessage(interviewId, userId, questionNo, questionContent, difficulty);
        updateInterviewAsInProgress(interviewId, questionNo, LocalDateTime.now());
    }

    /** 持久化当前题目的助手回复。 */
    @Transactional
    public void saveAssistantMessage(String interviewId, Long userId, Integer questionNo, String content) {
        insertAssistantMessage(interviewId, userId, questionNo, content);
    }

    /** 完成上一题并持久化下一题，保证同一事务提交或回滚。 */
    @Transactional
    public void completeQuestionAndSaveNext(String interviewId, Long userId, Integer completedQuestionNo,
                                            Map<String, Object> evaluation, Integer nextQuestionNo,
                                            String questionContent, String difficulty) {
        updateCompletedQuestion(interviewId, completedQuestionNo, evaluation);
        insertQuestionAndAssistantMessage(interviewId, userId, nextQuestionNo, questionContent, difficulty);
        updateInterviewAsInProgress(interviewId, nextQuestionNo, null);
    }

    /** 完成最后一题、写入总评消息并结束面试。 */
    @Transactional
    public void completeQuestionAndInterview(String interviewId, Long userId, Integer completedQuestionNo,
                                             Map<String, Object> evaluation) {
        if (evaluation == null) {
            throw new IllegalArgumentException("最终题目评价不能为空");
        }
        Object feedback = evaluation.get("feedback");
        if (!(feedback instanceof String content) || content.isBlank()) {
            throw new IllegalArgumentException("最终题目评价缺少有效的总体反馈");
        }
        updateCompletedQuestion(interviewId, completedQuestionNo, evaluation);
        insertAssistantMessage(interviewId, userId, completedQuestionNo, content);
        int updated = interviewMapper.updateInterviewFieldsByInterviewId(
                interviewId,
                InterviewStatus.COMPLETED.name(),
                completedQuestionNo,
                LocalDateTime.now(),
                null
        );
        if (updated != 1) {
            throw new IllegalStateException("未能更新面试完成状态，interviewId=" + interviewId);
        }
    }

    private void updateCompletedQuestion(String interviewId, Integer questionNo,
                                         Map<String, Object> evaluation) {
        if (questionNo == null || evaluation == null || !(evaluation.get("score") instanceof Number score)) {
            throw new IllegalArgumentException("题目评价缺少有效的题号或评分");
        }
        int scoreValue;
        try {
            scoreValue = new BigDecimal(score.toString()).intValueExact();
        } catch (ArithmeticException | NumberFormatException e) {
            throw new IllegalArgumentException("题目评分必须是 0 到 100 的整数", e);
        }
        if (scoreValue < 0 || scoreValue > 100) {
            throw new IllegalArgumentException("题目评分必须是 0 到 100 的整数");
        }
        String evaluationJson = objectMapper.writeValueAsString(evaluation);
        int updated = questionMapper.updateQuestionByInterviewIdAndQuestionNo(
                interviewId,
                questionNo,
                BigDecimal.valueOf(scoreValue),
                evaluationJson,
                InterviewQuestionStatus.COMPLETED.name()
        );
        if (updated != 1) {
            throw new IllegalStateException("未能唯一更新已完成的面试题，questionNo=" + questionNo);
        }
    }

    private void insertQuestionAndAssistantMessage(String interviewId, Long userId, Integer questionNo,
                                                    String questionContent, String difficulty) {
        InterviewQuestion question = InterviewQuestion.builder()
                .interviewId(interviewId)
                .userId(userId)
                .questionNo(questionNo)
                .questionContent(questionContent)
                .difficulty(difficulty)
                .status(InterviewQuestionStatus.PENDING.name())
                .build();
        if (questionMapper.insertInterviewQuestion(question) != 1) {
            throw new IllegalStateException("未能保存面试题，interviewId=" + interviewId
                    + ", questionNo=" + questionNo);
        }
        insertAssistantMessage(interviewId, userId, questionNo, questionContent);
    }

    private void updateInterviewAsInProgress(String interviewId, Integer questionCount,
                                             LocalDateTime startedAt) {
        int updated = interviewMapper.updateInterviewFieldsByInterviewId(
                interviewId,
                InterviewStatus.IN_PROGRESS.name(),
                questionCount,
                null,
                startedAt
        );
        if (updated != 1) {
            throw new IllegalStateException("未能更新面试进度，interviewId=" + interviewId);
        }
    }

    private void insertAssistantMessage(String interviewId, Long userId, Integer questionNo, String content) {
        InterviewMessage message = InterviewMessage.builder()
                .interviewId(interviewId)
                .userId(userId)
                .questionNo(questionNo)
                .role(Role.ASSISTANT.name())
                .content(content)
                .build();
        if (messageMapper.insertInterviewMessage(message) != 1) {
            throw new IllegalStateException("未能保存助手消息，interviewId=" + interviewId
                    + ", questionNo=" + questionNo);
        }
    }
}
