package flcr.backend.common.config;

import cn.binarywang.wx.miniapp.api.impl.BaseWxMaServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.beans.factory.config.BeanPostProcessor;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.lang.reflect.Field;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

@Slf4j
@Configuration
@Profile("cloud")
public class WxMaSslConfiguration implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof BaseWxMaServiceImpl<?, ?> service) {
            configurePermissiveSsl(service);
        }
        return bean;
    }

    private void configurePermissiveSsl(BaseWxMaServiceImpl<?, ?> service) {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null,
                    new TrustManager[]{new TrustAllManager()},
                    new SecureRandom());

            CloseableHttpClient httpClient = HttpClients.custom()
                    .setSSLContext(sslContext)
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .setMaxConnTotal(100)
                    .setMaxConnPerRoute(50)
                    .build();

            Field field = findHttpClientField(service.getClass());
            if (field != null) {
                field.setAccessible(true);
                field.set(service, httpClient);
                log.info("WeChat SDK SSL configured for cloud environment (trust-all mode)");
            } else {
                log.warn("Could not find httpClient field in BaseWxMaServiceImpl, SSL fix not applied");
            }
        } catch (Exception e) {
            log.error("Failed to configure WeChat SDK SSL for cloud environment", e);
        }
    }

    private Field findHttpClientField(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (CloseableHttpClient.class.isAssignableFrom(field.getType())) {
                return field;
            }
        }
        Class<?> superClass = clazz.getSuperclass();
        return superClass != null ? findHttpClientField(superClass) : null;
    }

    private static class TrustAllManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
}
