package known.my.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class KnowledgeFileUploadDTO {
    @NotNull(message = "文件不能为空")
    private MultipartFile file;

    @NotBlank(message = "分类不能为空")
    private String category;

    /**
     * 可见性
     * private: 仅自己可见
     * public: 所有人可见
     */
    @NotBlank(message = "可见性不能为空")
    @Pattern(regexp = "private|public", message = "可见性必须是 private 或 public")
    private String visibility;
}
