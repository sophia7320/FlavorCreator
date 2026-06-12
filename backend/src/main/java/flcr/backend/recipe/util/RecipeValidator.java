package flcr.backend.recipe.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import flcr.backend.common.constants.ResultCode;
import flcr.backend.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class RecipeValidator {

    private final ObjectMapper objectMapper;

    private static final Set<String> VALID_CATEGORIES = Set.of("fast", "lowcal", "home", "special", "health");

    public void validateCategory(String category) {
        if (!VALID_CATEGORIES.contains(category)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "无效的分类：" + category);
        }
    }

    public void validateJsonField(String json, String fieldName) {
        if (json != null && !json.isBlank()) {
            try {
                objectMapper.readTree(json);
            } catch (Exception e) {
                throw new BusinessException(ResultCode.PARAM_ERROR, fieldName + "格式不正确");
            }
        }
    }
}
