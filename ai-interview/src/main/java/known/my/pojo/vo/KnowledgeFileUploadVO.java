package known.my.pojo.vo;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class KnowledgeFileUploadVO {
    private String fileName;

    private String fileId;

    private Long fileSize;
}
