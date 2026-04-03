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
        return new WxMaDefaultConfigImpl() {
            // 1. 【注入】注入 WxMaConfig 对象
            {
                this.setAppid(appId);
                this.setSecret(secret); // 虽然免Token，但配置里最好还是填上，库内部可能需要
            }

            // 2. 【核心】重写 getAccessToken 方法
            @Override
            public String getAccessToken() {
                // 微信云托管会将 token 推送到这个固定路径
                String tokenPath = "/.tencentcloudbase/wx/cloudbase_access_token";
                try {
                    // 直接读取文件内容
                    String token = new String(Files.readAllBytes(Paths.get(tokenPath)));
                    // 注意：读取到的内容可能包含换行符，建议 trim()
                    return token.trim();
                } catch (IOException e) {
                    // 如果读取失败（比如本地没部署这个文件），可以选择 fallback 到普通模式
                    // 或者抛出异常提醒你检查环境
                    log.error("读取微信访问令牌文件失败: {}", tokenPath, e);
                    // 这里为了演示，如果读不到文件，就返回 null 或调用父类方法（父类会尝试去请求微信接口）
                    return super.getAccessToken();
                }
            }
        };
    }

    @Bean
    public WxMaService wxMaService(WxMaDefaultConfigImpl wxMaConfig) {
        WxMaService wxMaService = new WxMaServiceImpl();
        wxMaService.setWxMaConfig(wxMaConfig);
        return wxMaService;
    }

}
