package known.my.pojo.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgentInterviewContinueRequest {
    @JsonProperty("interview_id")
    private String interviewId;
    @JsonProperty("answer")
    private String answer;
}
