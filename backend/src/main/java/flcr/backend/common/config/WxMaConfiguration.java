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
        // 启动时诊断：打印 token 文件是否存在
        List<String> tokenPaths = List.of(
                "/wx/cloudbase_access_token",
                "/.tencentcloudbase/wx/cloudbase_access_token"
        );
        for (String path : tokenPaths) {
            java.io.File f = new java.io.File(path);
            log.info("云托管 token 文件检查: path={}, exists={}, readable={}, size={}",
                    path, f.exists(), f.canRead(), f.exists() ? f.length() : 0);
        }

        WxMaDefaultConfigImpl config = new WxMaDefaultConfigImpl() {
            @Override
            public String getAccessToken() {
                // 每次调用时动态读取，因为文件可能在容器启动后才被注入
                Optional<String> cloudToken = readCloudBaseToken();
                if (cloudToken.isPresent()) {
                    return cloudToken.get();
                }
                // fallback 到父类（可能返回 null，触发 SDK 自动刷新）
                return super.getAccessToken();
            }

            @Override
            public boolean isAccessTokenExpired() {
                // 如果能读到云托管文件，认为永不过期（由平台自动刷新）
                if (readCloudBaseToken().isPresent()) {
                    return false;
                }
                return super.isAccessTokenExpired();
            }
        };
        config.setAppid(appId);
        config.setSecret(secret);
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
