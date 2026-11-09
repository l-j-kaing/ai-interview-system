package known.my.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Builder
@Data
public class InterviewContinueVO {
    private String question;
    //面试问题编号
    private Integer questionNo;
    //面试状态
    private String status;
    private Integer completedQuestionNo;
    private Map<String, Object> questionEvaluation;
}
