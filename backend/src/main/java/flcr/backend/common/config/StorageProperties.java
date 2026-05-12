package flcr.backend.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "flcr.storage")
public class StorageProperties {
    private String type = "local";
    private String localPath = "./uploads";
    private String urlPrefix = "/uploads";
    private Cos cos = new Cos();

    @Data
    public static class Cos {
        private String secretId;
        private String secretKey;
        private String region = "ap-shanghai";
        private String bucket;
    }
}
