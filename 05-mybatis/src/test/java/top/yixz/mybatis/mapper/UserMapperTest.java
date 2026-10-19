package top.yixz.mybatis.mapper;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import top.yixz.mybatis.entity.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author yxzhang
 * @date 2026/10/10
 * @description UserMapperTest
 **/
@SpringBootTest
@Slf4j
class UserMapperTest {

    @Resource
    private UserMapper userMapper;

    @Test
    void testInsert() {
        User user = newUser("mapper_insert_" + System.currentTimeMillis());
        int result = userMapper.insert(user);
        log.info("{} 条数据插入成功，id = {}", result, user.getId());
    }

    @Test
    void testSearch() {
        User user = newUser("mapper_search_" + System.currentTimeMillis());
        userMapper.insert(user);

        User found = userMapper.selectById(user.getId());
        log.info("用户信息: {}", found);
        assertNotNull(found);
    }

    @Test
    void testDelete() {
        User user = newUser("mapper_delete_" + System.currentTimeMillis());
        userMapper.insert(user);

        // deleteById 返回的是影响行数，删一行就是 1
        int result = userMapper.deleteById(user.getId());
        assertEquals(1, result);
        log.info("{} 条数据删除成功", result);
    }

    private User newUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92");
        user.setAge(20);
        user.setVip(false);
        user.setCreateTime(LocalDateTime.now());
        return user;
    }
}
