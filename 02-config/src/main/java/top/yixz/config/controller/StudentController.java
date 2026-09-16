package top.yixz.config.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.yixz.config.properties.StudentProperties;

/**
 * @author yxzhang
 * @date 2026/9/16
 * @description StudentController
 **/
@RestController
@RequestMapping("/student")
@AllArgsConstructor
public class StudentController {
    private final StudentProperties studentProperties;
    @GetMapping("/info")
    public StudentProperties getStudent(){
        return studentProperties;
    }
}
