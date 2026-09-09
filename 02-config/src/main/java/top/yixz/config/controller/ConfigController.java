package top.yixz.config.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yxzhang
 * @date 2026/9/9
 * @description ConfigController
 **/
@RestController
@RequestMapping("/config")
public class ConfigController {
    @Value("${server.port}")
    private Integer servePort;
    @Value("${spring.application.name}")
    private String appName;
    @Value("${yixz.name}")
    private String Myname;
    @Value("${yixz.job}")
    private String Myjob;
    @GetMapping("/basic")
    public String getBasicInfo(){
        return "服务器端口是："+ this.servePort + "，应用名称是：" + appName;
    }

    @GetMapping("/my")
    public String getMyInfo(){
        return "我的姓名是："+ this.Myname + "我的职业是：" + Myjob;
    }


}
