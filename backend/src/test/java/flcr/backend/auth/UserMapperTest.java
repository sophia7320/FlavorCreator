package flcr.backend.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import flcr.backend.auth.entity.User;
import flcr.backend.auth.mapper.UserMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserMapper 单元测试
 * 每个测试方法在独立事务中运行，完成后自动回滚，不污染数据库
 */
@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    /**
     * 测试插入用户
     */
    @Test
    @DisplayName("测试插入用户")
    void testInsert() {
        User user = buildUser("测试用户", "test_openid_" + System.currentTimeMillis());
        int result = userMapper.insert(user);

        assertEquals(1, result);
        assertNotNull(user.getId());
        assertTrue(user.getId() > 0);
    }

    /**
     * 测试根据ID查询用户
     */
    @Test
    @DisplayName("测试根据ID查询用户")
    void testSelectById() {
        User user = buildUser("查询测试", "select_openid_" + System.currentTimeMillis());
        userMapper.insert(user);

        User found = userMapper.selectById(user.getId());

        assertNotNull(found);
        assertEquals("查询测试", found.getNickname());
        assertEquals(1, found.getGender());
    }

    /**
     * 测试更新用户
     */
    @Test
    @DisplayName("测试更新用户")
    void testUpdateById() {
        User user = buildUser("原始昵称", "update_openid_" + System.currentTimeMillis());
        userMapper.insert(user);

        user.setNickname("更新后的用户");
        user.setUpdatedAt(LocalDateTime.now());
        int result = userMapper.updateById(user);

        assertEquals(1, result);

        User updated = userMapper.selectById(user.getId());
        assertEquals("更新后的用户", updated.getNickname());
    }

    /**
     * 测试使用 LambdaQueryWrapper 查询
     */
    @Test
    @DisplayName("测试使用LambdaQueryWrapper查询")
    void testSelectWithLambdaQuery() {
        String openid = "lambda_openid_" + System.currentTimeMillis();
        User user = buildUser("Lambda查询测试", openid);
        userMapper.insert(user);

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getGender, 1)
               .eq(User::getOpenid, openid);

        List<User> users = userMapper.selectList(wrapper);

        assertNotNull(users);
        assertFalse(users.isEmpty());
        assertEquals("Lambda查询测试", users.get(0).getNickname());
    }

    /**
     * 测试根据 openid 查询
     */
    @Test
    @DisplayName("测试根据openid查询")
    void testSelectByOpenid() {
        String expectedOpenid = "by_openid_" + System.currentTimeMillis();
        User user = buildUser("Openid查询", expectedOpenid);
        userMapper.insert(user);

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOpenid, expectedOpenid);
        User found = userMapper.selectOne(wrapper);

        assertNotNull(found);
        assertEquals(expectedOpenid, found.getOpenid());
    }

    /**
     * 测试查询所有用户
     */
    @Test
    @DisplayName("测试查询所有用户")
    void testSelectAll() {
        // 先插入当前测试的数据
        userMapper.insert(buildUser("全部查询1", "all1_" + System.currentTimeMillis()));
        userMapper.insert(buildUser("全部查询2", "all2_" + System.currentTimeMillis()));

        List<User> users = userMapper.selectList(null);

        assertNotNull(users);
        assertTrue(users.size() >= 2);
    }

    /**
     * 测试删除用户
     */
    @Test
    @DisplayName("测试删除用户")
    void testDeleteById() {
        User user = buildUser("待删除", "delete_openid_" + System.currentTimeMillis());
        userMapper.insert(user);
        Long id = user.getId();

        // 确认存在
        assertNotNull(userMapper.selectById(id));

        // 删除
        int result = userMapper.deleteById(id);
        assertEquals(1, result);

        // 确认已删除
        assertNull(userMapper.selectById(id));
    }

    /**
     * 测试批量插入
     */
    @Test
    @DisplayName("测试批量插入")
    void testBatchInsert() {
        String batchKey = "batch_" + System.currentTimeMillis();
        for (int i = 0; i < 3; i++) {
            User user = buildUser("批量用户" + (i + 1),
                    batchKey + "_" + i);
            user.setGender(i % 2 == 0 ? 1 : 2);
            userMapper.insert(user);
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(User::getOpenid, batchKey);
        List<User> users = userMapper.selectList(wrapper);

        assertEquals(3, users.size());
    }

    /**
     * 测试统计用户数量
     */
    @Test
    @DisplayName("测试统计用户数量")
    void testCountUsers() {
        long before = userMapper.selectCount(null);

        userMapper.insert(buildUser("计数测试", "count_" + System.currentTimeMillis()));

        long after = userMapper.selectCount(null);
        assertEquals(before + 1, after);
    }

    /**
     * 构造测试用户
     */
    private User buildUser(String nickname, String openid) {
        User user = new User();
        user.setNickname(nickname);
        user.setGender(1);
        user.setOpenid(openid);
        user.setAvatar("https://example.com/avatar.jpg");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }
}
