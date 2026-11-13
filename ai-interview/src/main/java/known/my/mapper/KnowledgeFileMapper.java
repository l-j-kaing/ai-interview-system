package known.my.mapper;

import known.my.pojo.entity.KnowledgeFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface KnowledgeFileMapper {

    /**
     * 插入知识库文件
     */
    int insertKnowledgeFile(KnowledgeFile knowledgeFile);

    /**
     * 查询用户的所有知识库文件
     */
    List<KnowledgeFile> selectKnowledgeFilesByUserId(@Param("userId") Long userId);

    /**
     * 查询用户的指定知识库文件
     */
    KnowledgeFile selectKnowledgeFileByUserIdAndFileId(
            @Param("userId") Long userId,
            @Param("fileId") String fileId
    );

    /**
     * 查询用户指定知识库文件的非存储、非时间信息
     */
    KnowledgeFile selectKnowledgeFileInfoByUserIdAndFileId(
            @Param("userId") Long userId,
            @Param("fileId") String fileId
    );

    /**
     * 更新分类、可见性、存储路径和处理状态
     */
    int updateKnowledgeFile(KnowledgeFile knowledgeFile);
}
