package flcr.backend.common.constants;

import flcr.backend.common.exception.BusinessException;
import lombok.Getter;

/**
 * 图片上传场景枚举。
 * value 对应 ModerationProperties.maxSize 的 key 和 FileStorageService.store 的 dir。
 */
@Getter
public enum ImageScene {

    AVATAR("avatar"),
    BACKGROUND("background"),
    RECIPE_COVER("recipe-cover"),
    RECIPE_IMAGE("recipe-image");

    private final String value;

    ImageScene(String value) {
        this.value = value;
    }

    public static ImageScene fromValue(String value) {
        for (ImageScene scene : values()) {
            if (scene.value.equals(value)) {
                return scene;
            }
        }
        throw new BusinessException(ResultCode.IMAGE_SCENE_ERROR, "无效的上传场景: " + value);
    }
}
