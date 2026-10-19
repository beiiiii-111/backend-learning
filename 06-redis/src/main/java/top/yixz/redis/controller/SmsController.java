package top.yixz.redis.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.yixz.redis.common.Result;
import top.yixz.redis.dto.LoginRequest;
import top.yixz.redis.dto.LoginResponse;
import top.yixz.redis.service.SmsService;

/**
 * @author yxzhang
 * @date 2026/10/19
 * @description SmsController
 **/
@RestController
@RequestMapping("/sms")
@RequiredArgsConstructor
public class SmsController {
    private final SmsService smsService;

    @PostMapping("/send")
    public Result<String> send(@RequestParam String phone) {
        smsService.sendSms(phone);
        return Result.ok("短信发送成功");
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        return Result.ok(smsService.login(loginRequest));
    }
}
