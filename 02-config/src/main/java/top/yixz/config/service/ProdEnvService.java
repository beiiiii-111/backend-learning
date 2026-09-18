package top.yixz.config.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * @author yxzhang
 * @date 2026/9/16
 * @description DevEnvService
 * <p>
 **/
@Service
@Profile("prod")
public class ProdEnvService implements EnvService{
    @Override
    public String envInfo(){
        return "我是 Prod 环境专属的 Bean";
    }
}