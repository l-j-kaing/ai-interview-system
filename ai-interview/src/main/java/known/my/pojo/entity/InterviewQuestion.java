package known.my.pojo.entity;

import lombok.Data;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 面试问题及回答评估实体，对应 interview_questions 表。
 */
@Data
@Builder
public class InterviewQuestion {
    /** 问题记录主键。 */
    private Long id;
    /** 所属面试记录 ID。 */
    private String interviewId;
    /** 所属用户 ID。 */
    private Long userId;
    /** 问题在本次面试中的序号。 */
    private Integer questionNo;
    /** 面试问题文本。 */
    private String questionContent;
    /** 本题难度。 */
    private String difficulty;
    /** 本题状态。 */
    private String status;
    /** 本题评分。 */
    private BigDecimal score;
    /** 本题评估结果。 */
    private String evaluation;
    /** 记录创建时间。 */
    private LocalDateTime createdAt;
    /** 记录最后更新时间。 */
    private LocalDateTime updatedAt;
}
