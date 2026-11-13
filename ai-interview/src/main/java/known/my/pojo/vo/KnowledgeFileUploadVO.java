package known.my.pojo.vo;

import lombok.Builder;

@Builder
public class KnowledgeFileUploadVO {
    private String fileName;

    private String fileId;

    private Long fileSize;
}
