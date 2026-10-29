package top.yixz.web.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.yixz.web.filter.LogFilter;
import top.yixz.web.filter.TimeFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * 过滤器配置
 *
 * @author mqxu
 * @date 2026/10/28
 * @description FilterConfig
 */
@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<LogFilter> logFilterFilterRegistrationBean() {
        FilterRegistrationBean<LogFilter> registration = new FilterRegistrationBean<>();
        // 设置过滤器实例
        registration.setFilter(new LogFilter());
        // 设置拦截路径（/* 匹配所有请求）
        List<String> urlPatterns = new ArrayList<>();
        urlPatterns.add("/api/log/hello");
        registration.setUrlPatterns(urlPatterns);
        // 设置执行顺序（值越小越先执行）
        registration.setOrder(3);
        // 排除静态资源（可选）
        registration.addInitParameter("exclusions", "*.js,*.css,*.png");
        return registration;
    }

    @Bean
    public FilterRegistrationBean<TimeFilter> timeFilterFilterRegistrationBean() {
        FilterRegistrationBean<TimeFilter> registration = new FilterRegistrationBean<>();
        // 设置过滤器实例
        registration.setFilter(new TimeFilter());
        // 拦截所有请求
        registration.addUrlPatterns("/*");
        // 值越小越先执行，LogFilter 是 2，所以 TimeFilter 排在它后面
        registration.setOrder(1);
        return registration;
    }
}
