package known.my.pojo.response;

import lombok.Data;

import java.util.List;
@Data
public class RagChunkResponse {
    private List<String> chunks;
}
