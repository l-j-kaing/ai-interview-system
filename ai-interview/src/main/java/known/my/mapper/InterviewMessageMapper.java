package known.my.mapper;

import known.my.pojo.entity.InterviewMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 面试对话消息表的数据访问接口。
 */
@Mapper
public interface InterviewMessageMapper {
    /** 插入一条对话消息，并回填生成的主键。 */
    int insertInterviewMessage(InterviewMessage message);

    /** 按对话顺序查询指定面试的所有消息。 */
    List<InterviewMessage> selectMessagesByInterviewId(@Param("interviewId") String interviewId);
}
