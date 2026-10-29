package top.yixz.web.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * 日志过滤器
 *
 * @author mqxu
 * @date 2026/10/28
 * @description LogFilter
 */
@Slf4j
public class LogFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("LogFilter 过滤器初始化");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        // 获取客户端的请求方法
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String method = request.getMethod();
        log.info("请求方法是：{}，请求时间是：{}", method, LocalDateTime.now());
        // 获取请求参数（GET 是 url 后面的查询串）
        log.info("请求参数是：{}", request.getQueryString());
        // 执行并移交至下一个过滤器
        chain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
        log.info("LogFilter 过滤器销毁");
    }
}
