package flcr.backend.common.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * 容错整数反序列化器——处理 LLM 返回的带后缀数字，如 "30分钟"、"250大卡"。
 *
 * <p>正数 → 直接返回；字符串 → 去除非数字字符后解析。</p>
 */
public class FlexibleIntegerDeserializer extends JsonDeserializer<Integer> {

    @Override
    public Integer deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.getCurrentToken().isNumeric()) {
            return p.getIntValue();
        }
        String raw = p.getText();
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return null;
        }
        return Integer.parseInt(digits);
    }
}
