package flcr.backend.common.context;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("用户上下文测试")
class UserContextTest {

    @Test
    @DisplayName("设置和获取userId")
    void testSetGetUserId_PersistsAcrossSameThread() {
        UserContext.setUserId(1L);
        assertEquals(1L, UserContext.getUserId());
    }

    @Test
    @DisplayName("clear后userId为null")
    void testClearUserId_RemovesUserId() {
        UserContext.setUserId(1L);
        UserContext.clear();
        assertNull(UserContext.getUserId());
    }
}
