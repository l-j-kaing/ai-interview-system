package known.my.pojo.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;
@Data
@Builder
public class RagVectorRequest {

    private List<String> category;


    private List<String> chunks;
    @JsonProperty("file_id")
    private String fileId;

    @JsonProperty("user_id")
    private Integer userId;

    private String visibility;
}
