package flcr.backend.common.context;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserContextTest {

    @Test
    @DisplayName("设置和获取userId")
    void testSetGetUserId() {
        UserContext.setUserId(1L);
        assertEquals(1L, UserContext.getUserId());
    }

    @Test
    @DisplayName("设置和获取jti")
    void testSetGetJti() {
        UserContext.setJti("test-jti-uuid");
        assertEquals("test-jti-uuid", UserContext.getJti());
    }

    @Test
    @DisplayName("clear后userId为null")
    void testClearUserId() {
        UserContext.setUserId(1L);
        UserContext.clear();
        assertNull(UserContext.getUserId());
    }

    @Test
    @DisplayName("clear后jti为null")
    void testClearJti() {
        UserContext.setJti("test-jti");
        UserContext.clear();
        assertNull(UserContext.getJti());
    }

    @Test
    @DisplayName("clear清空所有ThreadLocal")
    void testClearBoth() {
        UserContext.setUserId(1L);
        UserContext.setJti("test");
        UserContext.clear();
        assertNull(UserContext.getUserId());
        assertNull(UserContext.getJti());
    }
}
