package flcr.backend;

import flcr.backend.auth.entity.User;
import flcr.backend.auth.mapper.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@DisplayName("应用启动测试")
class BackendApplicationTests {

    @Autowired
    private UserMapper userMapper;

    @Test
    @DisplayName("测试上下文加载成功")
    void testContextLoads_Success() {

    }
}
