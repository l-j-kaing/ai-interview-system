package known.my.pojo.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;
@Data
@Builder
public class RagChunkRequest {
    @JsonProperty("file_path")
    private String filePath;
    @JsonProperty("chunk_size")
    private Integer chunkSize;
    @JsonProperty("chunk_overlap")
    private Integer chunkOverlap;
    @JsonProperty("split_punctuations")
    private List<String> splitPunctuations;

    //用户id
    @JsonProperty("user_id")
    private Integer userId;

    //文件id
    @JsonProperty("file_id")
    private String fileId;
}