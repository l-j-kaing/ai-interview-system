package known.my.pojo.vo;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class InterviewStartVO {
    private String interviewId;

    private  Integer questionNo;

    private String content;
}
