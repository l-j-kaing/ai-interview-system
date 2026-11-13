package known.my.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ChunkByPunctuationVO {
    private List<String> chunks;

    private String category;
}
