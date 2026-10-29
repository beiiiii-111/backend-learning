package top.yixz.web.filter;

import jakarta.servlet.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * 计时过滤器
 *
 * @author mqxu
 * @date 2026/10/28
 * @description TimeFilter
 */
@Slf4j
public class TimeFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        log.info("TimeFilter 开始计时");
        long start = System.currentTimeMillis();
        // 放行，等后面的过滤器和接口都执行完再回来计时
        chain.doFilter(request, response);
        long cost = System.currentTimeMillis() - start;
        log.info("本次请求耗时：{}ms", cost);
    }
}
