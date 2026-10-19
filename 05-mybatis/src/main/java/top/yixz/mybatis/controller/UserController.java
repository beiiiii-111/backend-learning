package top.yixz.mybatis.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.yixz.mybatis.entity.User;
import top.yixz.mybatis.service.UserService;

import java.util.List;

/**
 * @author yxzhang
 * @date 2026/10/10
 * @description UserController
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** 新增。age 传 200 会抛异常 → 500，用于验证事务回滚 */
    @PostMapping
    public User create(@RequestBody User user) {
        userService.createUser(user);
        return user;
    }

    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) {
        return userService.getUser(id);
    }

    @GetMapping
    public List<User> list() {
        return userService.list();
    }

    /** 练习 2：age > 18 且 vip = true */
    @GetMapping("/vip-adult")
    public List<User> vipAdult() {
        return userService.listVipAdults();
    }

    /** 课后基础 1：LambdaUpdateWrapper 按 username 更新邮箱 */
    @PutMapping("/email")
    public boolean updateEmail(@RequestParam String username, @RequestParam String email) {
        return userService.modifyEmailByUsername(username, email);
    }

    /** 课后进阶 2：占住连接 ms 毫秒，用来配合 maximum-pool-size=1 复现池耗尽 */
    @GetMapping("/slow")
    public String slow(@RequestParam(defaultValue = "5000") long ms) {
        userService.slowQuery(ms);
        return "done after " + ms + "ms";
    }

    @GetMapping("/count")
    public long count() {
        return userService.count();
    }

    @PutMapping("/{id}")
    public User update(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        userService.modifyUser(user);
        return userService.getUser(id);
    }

    /** 逻辑删除 */
    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        return userService.removeUser(id);
    }
}
