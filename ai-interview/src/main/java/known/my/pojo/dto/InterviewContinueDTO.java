package known.my.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InterviewContinueDTO {
    @NotBlank(message = "面试ID不能为空")
    private String interviewId;

    @NotBlank(message = "回答不能为空")
    private String answer;
}
