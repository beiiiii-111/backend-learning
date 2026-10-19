package top.yixz.mybatis;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
// Spring Boot 4 把这个注解挪到了 webmvc.test 包下
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import top.yixz.mybatis.entity.User;
import top.yixz.mybatis.service.UserService;

import jakarta.annotation.Resource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 8.1 课堂随堂练习，5 条各一个用例
 *
 * @author yxzhang
 * @date 2026/10/10
 * @description UserClassExerciseTest
 */
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
class UserClassExerciseTest {

    @Resource
    private UserService userService;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MockMvc mockMvc;

    /** 练习 1：用 getById 查询一个不存在的 id → 返回 null，不报错 */
    @Test
    void practice1_getByIdNotExist() {
        User user = userService.getUser(999999L);
        log.info("练习1：查不存在的 id，结果 = {}", user);
        assertNull(user);
    }

    /** 练习 2：LambdaQueryWrapper 按 age > 18 且 vip = true 查询 → 两个条件都生效 */
    @Test
    void practice2_lambdaQueryAgeAndVip() {
        List<User> list = userService.listVipAdults();
        log.info("练习2：命中 {} 条", list.size());
        assertFalse(list.isEmpty());
        assertTrue(list.stream().allMatch(u -> u.getAge() > 18 && Boolean.TRUE.equals(u.getVip())));
    }

    /** 练习 3：新增 age = 200 的记录 → 接口返回 500，count 不变（事务回滚） */
    @Test
    void practice3_transactionRollback() throws Exception {
        long before = userService.count();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"age200\",\"age\":200}"))
                .andExpect(status().isInternalServerError());

        assertEquals(before, userService.count());
        log.info("练习3：回滚后 count 仍为 {}", before);
    }

    /** 练习 4：逻辑删除一条记录后 selectById → 返回 null，直接查库能看到 deleted = 1 */
    @Test
    void practice4_logicDelete() {
        User user = newUser("to_delete_" + System.currentTimeMillis());
        userService.createUser(user);
        Long id = user.getId();

        assertTrue(userService.removeUser(id));
        assertNull(userService.getUser(id));

        Integer deleted = jdbcTemplate.queryForObject(
                "select deleted from t_user where id = ?", Integer.class, id);
        assertEquals(1, deleted);
        log.info("练习4：id={} 已逻辑删除，库里 deleted = {}", id, deleted);
    }

    /**
     * 练习 5：乐观锁。
     * 注意：MP 更新成功后会把新版本号回填到实体上，所以「同一个对象连着调两次 updateById」
     * 第二次会拿着新 version 去匹配，反而会成功。要复现拦截，得用两份 version 相同的快照：
     * 第一份改完 version 0 -> 1，第二份还拿 0 去匹配，就对不上了。
     */
    @Test
    void practice5_optimisticLock() {
        User user = newUser("lock_" + System.currentTimeMillis());
        userService.createUser(user);
        Long id = user.getId();

        User first = userService.getUser(id);
        User stale = userService.getUser(id);
        log.info("练习5：两份快照的 version 都是 {}", first.getVersion());

        first.setNickname("第一次改");
        assertTrue(userService.modifyUser(first));

        stale.setNickname("第二次改");
        assertFalse(userService.modifyUser(stale), "version 已过期，应被乐观锁拦下");

        // 同一个对象连着改两次的真实结果（这里不断言，只打印出来看差异）
        User same = userService.getUser(id);
        same.setNickname("A");
        log.info("练习5：同一对象第一次 = {}", userService.modifyUser(same));
        same.setNickname("B");
        log.info("练习5：同一对象第二次 = {}（MP 回填了新 version，所以这里是 true）",
                userService.modifyUser(same));
    }

    private User newUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setAge(20);
        user.setVip(true);
        return user;
    }
}
