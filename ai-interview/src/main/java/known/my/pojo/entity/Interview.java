package known.my.pojo.entity;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 面试记录实体，对应 interviews 表。
 */
@Data
@Builder
public class Interview {
    /** 面试记录字符串主键。 */
    private String interviewId;
    /** 发起面试的用户 ID。 */
    private Long userId;
    /** 应聘岗位。 */
    private String jobPosition;
    /** 面试技术方向。 */
    private String techDirection;
    /** 面试难度模式。 */
    private String difficultyMode;
    /** 面试状态。 */
    private String status;
    /** 面试题目数量。 */
    private Integer questionCount;
    /** 面试开始时间。 */
    private LocalDateTime startedAt;
    /** 面试结束时间。 */
    private LocalDateTime finishedAt;
    /** 记录创建时间。 */
    private LocalDateTime createdAt;
    /** 记录最后更新时间。 */
    private LocalDateTime updatedAt;
}
