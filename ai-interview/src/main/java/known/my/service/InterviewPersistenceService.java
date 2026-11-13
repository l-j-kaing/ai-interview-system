package known.my.service;

import java.util.Map;

public interface InterviewPersistenceService {

    void saveFirstQuestion(String interviewId, Long userId, Integer questionNo,
                           String questionContent, String difficulty);

    void saveUserAnswer(String interviewId, Long userId, Integer questionNo, String content);

    void saveAssistantMessage(String interviewId, Long userId, Integer questionNo, String content);

    void completeQuestionAndSaveNext(String interviewId, Long userId, Integer completedQuestionNo,
                                     Map<String, Object> evaluation, Integer nextQuestionNo,
                                     String questionContent, String difficulty);

    void completeQuestionAndInterview(String interviewId, Long userId, Integer completedQuestionNo,
                                      Map<String, Object> evaluation);
}
