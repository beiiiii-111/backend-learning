package top.logging.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yxzhang
 * @date 2026/9/23
 * @description LoggingController
 **/
@RestController
@RequestMapping
@Slf4j
public class LoggingController {
    @GetMapping("/hello")
    public String hello() {
        return "日志管理 模块";
    }

    @GetMapping("/demo")
    public String demo(){
        log.info("处理 /log/demo 请求");
        log.warn("这是一条警告日志");
        log.error("这是一条错误日志");
        return "ok";
    }
}
