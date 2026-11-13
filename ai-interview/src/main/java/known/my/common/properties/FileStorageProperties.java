package known.my.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "known.my.knowledgeFile")
@Data
public class FileStorageProperties {
    private String localRoot;
}
