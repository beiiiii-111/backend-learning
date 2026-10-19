package top.yixz.mybatis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import top.yixz.mybatis.entity.User;
import top.yixz.mybatis.mapper.UserMapper;
import top.yixz.mybatis.service.UserService;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;

/**
 * @author yxzhang
 * @date 2026/10/10
 * @description UserServiceImpl
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final String DEFAULT_PASSWORD = "123456";

    @Override
    public User getUser(Long id) {
        return getById(id);
    }

    /**
     * 先写库再校验，用来演示事务：
     * 校验不通过抛异常时，前面已经执行的 insert 会被回滚。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean createUser(User user) {
        applyDefaults(user);
        save(user);

        // age 列是 TINYINT UNSIGNED，200 在数据库层面是合法的，
        // 这里校验的是业务上不合理的取值，抛异常观察前面的 insert 是否被回滚
        if (user.getAge() != null && (user.getAge() < 0 || user.getAge() > 150)) {
            throw new RuntimeException("年龄超出合理范围，回滚");
        }
        return true;
    }

    @Override
    public boolean modifyUser(User user) {
        if (user.getId() == null) {
            throw new IllegalArgumentException("更新时必须携带主键 id");
        }
        if (getById(user.getId()) == null) {
            throw new IllegalArgumentException("用户不存在：" + user.getId());
        }
        // 实体带 version 时，MP 会拼 AND version = ?；影响行数为 0 就返回 false
        return updateById(user);
    }

    @Override
    public boolean removeUser(Long id) {
        // 配了逻辑删除，实际执行的是 UPDATE ... SET deleted = 1
        return removeById(id);
    }

    @Override
    public List<User> listVipAdults() {
        return list(new LambdaQueryWrapper<User>()
                .gt(User::getAge, 18)
                .eq(User::getVip, true)
                .orderByAsc(User::getId));
    }

    /**
     * 课后基础 1：按 username 更新邮箱。
     * update(wrapper) 只带 SET 子句（set 指定的字段）和 WHERE 条件，
     * 生成的 SQL：UPDATE t_user SET email=?, update_time=? WHERE username=? AND deleted=0
     * 对比 updateById：那个只能按主键改，且要把整个实体传进来。
     */
    @Override
    public boolean modifyEmailByUsername(String username, String email) {
        if (!StringUtils.hasText(username)) {
            throw new IllegalArgumentException("username 不能为空");
        }
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("email 不能为空");
        }
        return update(new LambdaUpdateWrapper<User>()
                .eq(User::getUsername, username)
                .set(User::getEmail, email)
                // 踩坑：update(wrapper) 不带实体时，MetaObjectHandler 的 updateFill 不会触发，
                // update_time 不会刷新，只能显式 set
                .set(User::getUpdateTime, LocalDateTime.now()));
    }

    /**
     * 课后进阶 2：占住连接的慢请求。
     * 先执行一次查询拿到连接，再 sleep 模拟慢 SQL / 长事务；
     * 事务未结束连接不归还，池上限为 1 时第二个并发请求只能排队等 connection-timeout
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void slowQuery(long holdMillis) {
        list(); // 首次访问数据库，从池里取走连接
        try {
            Thread.sleep(holdMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** 给未传的字段补默认值，避免 NOT NULL 列报错；明文密码转 SHA-256 摘要 */
    private void applyDefaults(User user) {
        if (!StringUtils.hasText(user.getUsername())) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (!StringUtils.hasText(user.getPassword())) {
            user.setPassword(sha256(DEFAULT_PASSWORD));
        }
        if (user.getGender() == null) {
            user.setGender(0);
        }
        if (user.getMemberLevel() == null) {
            user.setMemberLevel("BRONZE");
        }
        if (user.getPoints() == null) {
            user.setPoints(0);
        }
        if (user.getBalance() == null) {
            user.setBalance(BigDecimal.ZERO);
        }
        if (user.getVip() == null) {
            user.setVip(false);
        }
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
    }

    private String sha256(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("当前 JDK 不支持 SHA-256", e);
        }
    }
}
