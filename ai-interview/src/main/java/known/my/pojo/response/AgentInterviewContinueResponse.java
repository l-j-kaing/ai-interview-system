package known.my.pojo.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class AgentInterviewContinueResponse {
    private String status;
    @JsonProperty("question_no")
    private Integer questionNo;

    private String question;

    private String difficulty;
    @JsonProperty("completed_question_no")
    private Integer completedQuestionNo;
    @JsonProperty("question_evaluation")
    private Map<String, Object> questionEvaluation;
}
