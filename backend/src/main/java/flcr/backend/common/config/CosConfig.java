package flcr.backend.common.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "flcr.storage.type", havingValue = "cos")
public class CosConfig {

    private final StorageProperties storageProperties;
    private COSClient cosClient;

    @Bean
    public COSClient cosClient() {
        StorageProperties.Cos cos = storageProperties.getCos();
        COSCredentials cred = new BasicCOSCredentials(cos.getSecretId(), cos.getSecretKey());
        ClientConfig clientConfig = new ClientConfig(new Region(cos.getRegion()));
        clientConfig.setConnectionTimeout(5000);
        clientConfig.setSocketTimeout(5000);
        clientConfig.setMaxConnectionsCount(100);
        cosClient = new COSClient(cred, clientConfig);
        log.info("COSClient 单例 Bean 创建成功");
        return cosClient;
    }

    @PreDestroy
    public void destroy() {
        if (cosClient != null) {
            cosClient.shutdown();
            log.info("COSClient 已关闭");
        }
    }
}
