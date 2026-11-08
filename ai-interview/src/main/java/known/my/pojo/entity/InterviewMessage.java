package known.my.pojo.entity;

import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 面试对话消息实体，对应 interview_messages 表。
 */
@Data
@Builder
public class InterviewMessage {
    /** 消息主键。 */
    private Long id;
    /** 所属面试记录 ID。 */
    private String interviewId;
    /** 所属用户 ID。 */
    private Long userId;
    /** 该消息关联的问题序号。 */
    private Integer questionNo;
    /** 消息角色，例如 interviewer 或 candidate。 */
    private String role;
    /** 消息文本内容。 */
    private String content;
    /** 消息创建时间。 */
    private LocalDateTime createdAt;
}
