package known.my.mapper;

import known.my.pojo.entity.Interview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 面试记录表的数据访问接口。
 */
@Mapper
public interface InterviewMapper {
    /** 插入一条面试记录。 */
    int insertInterview(Interview interview);

    /** 按主键查询面试记录。 */
    Interview selectInterviewById(@Param("interviewId") String interviewId);

    /** 查询指定用户的面试记录，按创建时间倒序排列。 */
    List<Interview> selectInterviewsByUserId(@Param("userId") Long userId);

    /** 更新面试状态、题目数量和时间字段；传入 null 的字段不更新。 */
    int updateInterviewFieldsByInterviewId(@Param("interviewId") String interviewId,
                                           @Param("status") String status,
                                           @Param("questionCount") Integer questionCount,
                                           @Param("finishedAt") LocalDateTime finishedAt,
                                           @Param("startedAt") LocalDateTime startedAt);
}
