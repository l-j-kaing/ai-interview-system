package known.my.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class InterviewStartDTO {
    @NotBlank(message = "职位不能为空")
    private String jobPosition;

    @NotBlank(message = "技术方向不能为空")
    private String techDirection;

    @NotBlank(message = "难度模式不能为空")
    @Pattern(regexp = "AUTO|EASY|MEDIUM|HARD", message = "难度模式必须为 AUTO、EASY、MEDIUM 或 HARD")
    private String difficultyMode;

    /** 本次面试的初始难度，需使用 EASY、MEDIUM 或 HARD。 */
    @NotBlank(message = "当前难度不能为空")
    @Pattern(regexp = "EASY|MEDIUM|HARD", message = "当前难度必须为 EASY、MEDIUM 或 HARD")
    private String currentDifficulty;
}
