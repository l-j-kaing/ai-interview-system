package known.my.client;

import known.my.pojo.request.RagChunkRequest;
import known.my.pojo.request.RagVectorRequest;
import known.my.pojo.response.RagChunkResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

@Component
public class PythonRagClient {
    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PythonRagClient(
            RestClient.Builder builder,
            @Value("${python.agent.base-url}") String baseUrl
    ) {
        this.restClient = builder
                .baseUrl(baseUrl)
                .requestFactory(new SimpleClientHttpRequestFactory())
                .build();
    }
    /**
     * 请求对知识文件进行切块
     */
    public RagChunkResponse chunkKnowledgeFile(RagChunkRequest request) {
        String jsonBody = objectMapper.writeValueAsString(request);
        return restClient.post()
                .uri("/api/knowledge/chunk/punctuation")
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonBody)
                .retrieve()
                .body(RagChunkResponse.class);
    }
    /**
     * 请求对知识块进行向量化
     */
    public void vectorizeKnowledgeChunks(RagVectorRequest request) {
        String jsonBody = objectMapper.writeValueAsString(request);
        restClient.post()
                .uri("/api/knowledge/vectorize")
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonBody)
                .retrieve()
                .body(Void.class);
    }

}
