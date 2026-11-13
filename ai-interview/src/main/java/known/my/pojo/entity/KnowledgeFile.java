package known.my.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeFile {

    /**
     * 主键
     */
    private Long id;

    /**
     * 所属用户ID
     */
    private Long userId;

    /**
     * 文件唯一标识
     */
    private String fileId;

    /**
     * 原始文件名称
     */
    private String fileName;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 文件大小，单位字节
     */
    private Long fileSize;

    /**
     * 知识分类
     */
    private String category;

    /**
     * 可见性：private / public
     */
    private String visibility;

    /**
     * 文件状态：1-上传成功，2-上传失败，3-切块成功，4-切块失败，5-向量化成功，6-向量化失败
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 存储路径
     */
    private String storagePath;

    /**
     * 存储类型
     */
    private String storageType;
}
