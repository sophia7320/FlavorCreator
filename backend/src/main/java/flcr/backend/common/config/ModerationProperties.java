package flcr.backend.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "flcr.moderation")
public class ModerationProperties {

    private boolean enabled = false;

    private List<String> allowedTypes = List.of("jpg", "jpeg", "png", "gif", "webp", "bmp");

    private Map<String, Long> maxSize = Map.of(
            "avatar", 2L * 1024 * 1024,
            "background", 5L * 1024 * 1024,
            "recipe-cover", 5L * 1024 * 1024,
            "recipe-image", 5L * 1024 * 1024
    );
}
