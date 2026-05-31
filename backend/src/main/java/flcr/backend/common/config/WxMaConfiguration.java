package flcr.backend.common.config;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Slf4j
@Configuration
public class WxMaConfiguration {

    @Value("${wx.app-id}")
    private String appId;

    @Value("${wx.secret:}")
    private String secret;

    @Bean
    public WxMaDefaultConfigImpl wxMaConfig() {
        WxMaDefaultConfigImpl config = new WxMaDefaultConfigImpl();
        config.setAppid(appId);
        config.setSecret(secret);

        // 云托管环境：初始化时一次性读取 access_token，避免每次 API 调用都读文件
        readCloudBaseToken().ifPresent(token -> {
            config.setAccessToken(token);
            log.info("已从云托管环境加载 access_token");
        });

        return config;
    }

    @Bean
    public WxMaService wxMaService(WxMaDefaultConfigImpl wxMaConfig) {
        WxMaService service = new WxMaServiceImpl();
        service.setWxMaConfig(wxMaConfig);
        return service;
    }

    private Optional<String> readCloudBaseToken() {
        // 云托管可能使用不同路径注入 token，尝试多个路径
        List<String> tokenPaths = List.of(
                "/wx/cloudbase_access_token",
                "/.tencentcloudbase/wx/cloudbase_access_token"
        );

        for (String path : tokenPaths) {
            try {
                Path tokenFile = Paths.get(path);
                if (Files.exists(tokenFile)) {
                    String token = Files.readString(tokenFile).trim();
                    if (!token.isEmpty()) {
                        log.debug("从 {} 读取到云托管 access_token", path);
                        return Optional.of(token);
                    }
                }
            } catch (IOException e) {
                log.debug("读取云托管 token 文件失败: {} - {}", path, e.getMessage());
            }
        }

        return Optional.empty();
    }

}
