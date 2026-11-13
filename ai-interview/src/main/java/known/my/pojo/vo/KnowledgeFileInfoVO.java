package known.my.pojo.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KnowledgeFileInfoVO {
    private Long id;

    private Long userId;

    private String fileId;

    private String fileName;

    private String fileType;

    private Long fileSize;

    private String category;

    private String visibility;

    private Integer status;
}
