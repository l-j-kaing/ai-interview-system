package known.my.pojo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
@Data
public class ChunkByPunctuationDTO {

    // 用户选择的切分标点
    @NotNull(message = "用户选择的切分标点不能为空")
    @Size(min = 1, message = "用户选择的切分标点不能为空")
    private List<String> splitPunctuations;

    // 每个 Chunk 最大字符数
    @NotNull(message = "每个 Chunk 最大字符数不能为空")
    @Size(min=100, max=1000, message = "每个 Chunk 最大字符数必须在 100 到 1000 之间")
    private Integer chunkSize;

    // 相邻 Chunk 重叠字符数
    @NotNull(message = "相邻 Chunk 重叠字符数不能为空")
    @Size(min=0, max=70, message = "相邻 Chunk 重叠字符数必须在 0 到 70 之间")
    private Integer chunkOverlap;
    // 文件ID
    @NotNull(message = "文件ID不能为空")
    private String fileId;

}
