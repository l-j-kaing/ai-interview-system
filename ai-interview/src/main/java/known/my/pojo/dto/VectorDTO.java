package known.my.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
@Data
public class VectorDTO {
    @NotNull(message = "类别不能为空")
    @Size(min = 1, message = "类别不能为空")
    private List<String> category;

    @NotNull(message = "切块不能为空")
    @Size(min = 1, message = "切块不能为空")
    private List<String> chunks;

    @NotBlank(message = "文件ID不能为空")
    private String fileId;

}
