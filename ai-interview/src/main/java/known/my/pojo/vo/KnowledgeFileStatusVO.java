package known.my.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class KnowledgeFileStatusVO {
    private List<String> fileIds;

    private List<Integer> status;
}
