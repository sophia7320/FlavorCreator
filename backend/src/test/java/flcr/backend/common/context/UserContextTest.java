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
    @DisplayName("clear后userId为null")
    void testClearUserId() {
        UserContext.setUserId(1L);
        UserContext.clear();
        assertNull(UserContext.getUserId());
    }
}
