package flcr.backend.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("容错整数反序列化器测试")
class FlexibleIntegerDeserializerTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Integer.class, new FlexibleIntegerDeserializer());
        mapper.registerModule(module);
    }

    @Test
    @DisplayName("纯数字字符串解析为整数")
    void testDeserialize_PureDigits() throws Exception {
        assertEquals(30, mapper.readValue("\"30\"", Integer.class));
    }

    @Test
    @DisplayName("带汉字后缀提取数字")
    void testDeserialize_WithChineseSuffix() throws Exception {
        assertEquals(30, mapper.readValue("\"30分钟\"", Integer.class));
    }

    @Test
    @DisplayName("带单位提取数字")
    void testDeserialize_WithUnitSuffix() throws Exception {
        assertEquals(250, mapper.readValue("\"250大卡\"", Integer.class));
    }

    @Test
    @DisplayName("JSON 数字直接返回")
    void testDeserialize_JsonNumber() throws Exception {
        assertEquals(10, mapper.readValue("10", Integer.class));
    }

    @Test
    @DisplayName("空字符串返回 null")
    void testDeserialize_EmptyString() throws Exception {
        assertNull(mapper.readValue("\"\"", Integer.class));
    }

    @Test
    @DisplayName("纯非数字返回 null")
    void testDeserialize_NoDigits() throws Exception {
        assertNull(mapper.readValue("\"分钟\"", Integer.class));
    }

    @Test
    @DisplayName("混合提取数字")
    void testDeserialize_MixedContent() throws Exception {
        assertEquals(123, mapper.readValue("\"约123kcal\"", Integer.class));
    }
}
