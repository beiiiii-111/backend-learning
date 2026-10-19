package top.yixz.mybatis.service;

import com.baomidou.mybatisplus.spring.service.IService;
import top.yixz.mybatis.entity.User;

import java.util.List;

/**
 * @author yxzhang
 * @date 2026/10/10
 * @description UserService
 */
public interface UserService extends IService<User> {

    /** 按主键查，查不到返回 null */
    User getUser(Long id);

    /** 新增，带默认值与业务校验，失败回滚 */
    boolean createUser(User user);

    /** 按主键更新，实体带 version 时走乐观锁 */
    boolean modifyUser(User user);

    /** 按主键删除，实际是逻辑删除 */
    boolean removeUser(Long id);

    /** 练习 2：age > 18 且 vip = true */
    List<User> listVipAdults();

    /**
     * 课后基础 1：用 LambdaUpdateWrapper 按 username 更新邮箱。
     * 条件字段用方法引用（User::getUsername），改字段名时编译器会报错，比手写 "username" 字符串安全
     */
    boolean modifyEmailByUsername(String username, String email);

    /**
     * 课后进阶 2：事务内先查库再 sleep，用于占住连接。
     * 事务方法在首次访问数据库后就把连接攥在手里，直到方法结束才归还，
     * maximum-pool-size = 1 时，一个请求在睡，其他请求 3 秒内拿不到连接就会报 Connection is not available
     */
    void slowQuery(long holdMillis);
}
