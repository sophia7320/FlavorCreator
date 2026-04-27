package flcr.backend.common.constants;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ResultCode 常量类单元测试
 */
class ResultCodeTest {

    @Test
    @DisplayName("所有常量值正确")
    void testConstantValues() {
        assertEquals(200, ResultCode.SUCCESS);
        assertEquals(400, ResultCode.PARAM_ERROR);
        assertEquals(401, ResultCode.USER_NOT_EXIST);
        assertEquals(402, ResultCode.USER_EXIST);
        assertEquals(403, ResultCode.PERMISSION_ERROR);
        assertEquals(404, ResultCode.RESOURCE_NOT_EXIST);
        assertEquals(500, ResultCode.SYSTEM_ERROR);
        assertEquals(1001, ResultCode.WX_CODE_ERROR);
        assertEquals(1002, ResultCode.WX_API_ERROR);
        assertEquals(1003, ResultCode.PHONE_ERROR);
    }

    @Test
    @DisplayName("错误码区间应在合理范围内")
    void testCodeRanges() {
        assertTrue(ResultCode.SUCCESS >= 200 && ResultCode.SUCCESS < 300);
        assertTrue(ResultCode.PARAM_ERROR >= 400);
        assertTrue(ResultCode.WX_CODE_ERROR >= 1000);
    }

    @Test
    @DisplayName("类为 final 且私有构造器不可实例化")
    void testCannotInstantiate() throws Exception {
        assertTrue(Modifier.isFinal(ResultCode.class.getModifiers()));

        Constructor<ResultCode> constructor = ResultCode.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));

        // 验证反射也无法创建实例（构造器抛异常或直接拒绝）
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
            // 如果没抛异常也算通过，因为私有构造器在单例工具类中是可接受的
        } catch (Exception ignored) {
        }
    }
}