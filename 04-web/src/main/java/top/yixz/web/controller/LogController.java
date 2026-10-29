package top.yixz.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.yixz.web.common.Result;

/**
 * 案例：日志过滤器演示接口
 *
 * @author mqxu
 * @date 2026/10/28
 * @description LogController
 */
@Slf4j
@RestController
@RequestMapping("/api/log")
public class LogController {

    @GetMapping("/hello")
    public Result<String> hello(@RequestParam String name) throws InterruptedException {
        // 线程休眠 200ms，模拟业务处理，让耗时统计更清晰
        Thread.sleep(200);
        log.info("LogController 收到请求，name={}", name);
        return Result.ok("hello, " + name);
    }
}
