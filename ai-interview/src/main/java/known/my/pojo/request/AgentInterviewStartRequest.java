package known.my.pojo.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgentInterviewStartRequest {

    @JsonProperty("interview_id")
    private String interviewId;

    @JsonProperty("job_position")
    private String jobPosition;

    @JsonProperty("tech_direction")
    private String techDirection;

    @JsonProperty("difficulty_mode")
    private String difficultyMode;

    @JsonProperty("current_difficulty")
    private String currentDifficulty;
}
