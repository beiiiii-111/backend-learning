package top.yixz.config.quickstart.controller;

import top.yixz.config.quickstart.entity.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * @author yxzhang
 * @date 2026/9/9
 * @description UserController
 **/
@RestController
@RequestMapping("/user")
public class UserController {

    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @GetMapping("/info")
    public User getUserInfo() {
        return new User(1000L, "张三", LocalDate.of(2000, 2, 4));
    }

    /**
     * 查询用户列表
     *
     * @return 全部用户
     */
    @GetMapping("/list")
    public List<User> listUsers() {
        return List.of(
                new User(1000L, "张三", LocalDate.of(2000, 2, 4)),
                new User(1001L, "李四", LocalDate.of(1999, 8, 15)),
                new User(1002L, "王五", LocalDate.of(2001, 6, 30))
        );
    }
}
