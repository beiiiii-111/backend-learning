package top.yixz.quickstart.controller;

import top.yixz.quickstart.entity.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * @author yxzhang
 * @date 2026/9/9
 * @description UserController
 **/
@RestController
@RequestMapping("/user")
public class UserController {
    @GetMapping("/info")
    public User getUserInfo(){
        return new User(1000L,"张三",LocalDate.of(2000,2,4));
    }
}
