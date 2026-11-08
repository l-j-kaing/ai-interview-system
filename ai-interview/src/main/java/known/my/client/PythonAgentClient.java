package known.my.client;

import known.my.pojo.request.AgentInterviewStartRequest;
import known.my.pojo.response.AgentInterviewStartResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Component
public class PythonAgentClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PythonAgentClient(
            RestClient.Builder builder,
            @Value("${python.agent.base-url}") String baseUrl
    ) {
        this.restClient = builder
                .baseUrl(baseUrl)
                .requestFactory(new SimpleClientHttpRequestFactory())
                .build();
    }


    /**
     * 请求 Python Agent 启动面试
     */
    public AgentInterviewStartResponse startInterview(
            AgentInterviewStartRequest request
    ) {
        Map<String, String> requestBody = Map.of(
                "interview_id", request.getInterviewId(),
                "job_position", request.getJobPosition(),
                "tech_direction", request.getTechDirection(),
                "difficulty_mode", request.getDifficultyMode(),
                "current_difficulty", request.getCurrentDifficulty()
        );
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        return restClient.post()
                .uri("/api/interview/start")
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonBody)
                .retrieve()
                .body(AgentInterviewStartResponse.class);
    }

}