package top.yixz.doc.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 全局文档信息：展示在 Swagger UI 顶部。
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bookOpenApi() {
        return new OpenAPI().info(new Info()
                .title("图书管理 API")
                .description("模块 10 接口文档综合练习：springdoc-openapi 自动生成 + 注解增强")
                .version("v1.0")
                .contact(new Contact().name("张心怡")));
    }
}
