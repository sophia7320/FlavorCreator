package flcr.backend;

import flcr.backend.auth.entity.User;
import flcr.backend.auth.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BackendApplicationTests {

    @Autowired
    private UserMapper userMapper;

    @Test
    void contextLoads() {

    }

}
