package top.yixz.web.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 接口耗时统计拦截器
 *
 * @author mqxu
 * @date 2026/10/28
 * @description TimeStatInterceptor
 */
@Slf4j
@Component
public class TimeStatInterceptor implements HandlerInterceptor {

    // 存放请求开始时间的键
    private static final String START_TIME = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 记录请求开始时间
        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME, startTime);
        // 放行请求
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // Controller 执行完成后（视图渲染前）
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求全部结束后，计算耗时
        long startTime = (long) request.getAttribute(START_TIME);
        long endTime = System.currentTimeMillis();
        long cost = endTime - startTime;
        // 打印日志（Handler 即当前正在处理的控制器）
        log.info("接口耗时 - 方法: {}，耗时: {}ms", handler.getClass().getSimpleName(), cost);
    }
}
