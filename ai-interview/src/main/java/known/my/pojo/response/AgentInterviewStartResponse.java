package known.my.pojo.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AgentInterviewStartResponse {
    @JsonProperty("interview_id")
    private String interviewId;

    private String question;

    private String difficulty;
    @JsonProperty("question_no")
    private Integer questionNo;

}
