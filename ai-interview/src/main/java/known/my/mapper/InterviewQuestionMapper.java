package known.my.mapper;

import known.my.pojo.entity.InterviewQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
 * 面试问题及回答评估表的数据访问接口。
 */
@Mapper
public interface InterviewQuestionMapper {
    /** 插入一条面试问题记录，并回填生成的主键。 */
    int insertInterviewQuestion(InterviewQuestion question);

    /** 按问题序号查询指定面试的问题。 */
    InterviewQuestion selectLatestQuestionByInterviewId(@Param("interviewId") String interviewId);

    /** 更新题目评分、评估结果和状态；传入 null 的字段不更新。 */
    int updateQuestionByInterviewIdAndQuestionNo(@Param("interviewId") String interviewId,
                                                 @Param("questionNo") Integer questionNo,
                                                 @Param("score") BigDecimal score,
                                                 @Param("evaluation") String evaluation,
                                                 @Param("status") String status);
}
