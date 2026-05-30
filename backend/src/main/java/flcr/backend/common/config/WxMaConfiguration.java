package flcr.backend.common.config;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
@Configuration
public class WxMaConfiguration {

    @Value("${wx.app-id}")
    private String appId;

    @Value("${wx.secret}")
    private String secret;

    @Bean
    public WxMaDefaultConfigImpl wxMaConfig() {
        // 创建自定义配置类
        WxMaDefaultConfigImpl config = new WxMaDefaultConfigImpl() {
            @Override
            public String getAccessToken() {
                String tokenPath = "/.tencentcloudbase/wx/cloudbase_access_token";
                try {
                    String token = new String(Files.readAllBytes(Paths.get(tokenPath)));
                    return token.trim();
                } catch (IOException e) {
                    log.warn("未读取到云托管 token 文件，回退到普通模式: {}", tokenPath);
                    return super.getAccessToken();
                }
            }
        };

        config.setAppid(appId);
        config.setSecret(secret);

        // 通过反射将 apiUrl 改为 http（绕过 SSL 证书问题）
        try {
            Field apiUrlField = WxMaDefaultConfigImpl.class.getDeclaredField("apiUrl");
            apiUrlField.setAccessible(true);
            apiUrlField.set(config, "http://api.weixin.qq.com");
            log.info("已将微信 API 地址强制设置为 http://api.weixin.qq.com");
        } catch (Exception e) {
            log.warn("反射修改 apiUrl 失败，当前 WxJava 版本可能不兼容", e);
        }

        return config;
    }

    @Bean
    public WxMaService wxMaService(WxMaDefaultConfigImpl wxMaConfig) {
        // 重要：使用普通的 WxMaServiceImpl，不要使用 WxMaCloudServiceImpl
        WxMaServiceImpl wxMaService = new WxMaServiceImpl();
        wxMaService.setWxMaConfig(wxMaConfig);
        return wxMaService;
    }
}