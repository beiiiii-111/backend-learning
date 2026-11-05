package top.yixz.security.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import top.yixz.security.security.JwtAuthenticationFilter;
import top.yixz.security.security.RestAccessDeniedHandler;
import top.yixz.security.security.RestAuthenticationEntryPoint;

/**
 * 安全配置
 *
 * @author yixz
 * @date 2026-10-29
 * @description 过滤链从具体到宽泛：登录和 ping 放行，/admin 要 ADMIN 角色，
 * 其余 authenticated 兜底。@EnableMethodSecurity 必须显式打开，
 * 漏了它所有 @PreAuthorize 被当成注释，不报错但形同虚设。
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 由自动配置组装：容器里有 UserDetailsService 和 PasswordEncoder，
     * Spring Boot 会自动配 DaoAuthenticationProvider，不用单独注册 Bean，
     * 否则启动时会有"UserDetailsService 不用于自动配置"的 warning
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtAuthenticationFilter,
                                                   RestAuthenticationEntryPoint authenticationEntryPoint,
                                                   RestAccessDeniedHandler accessDeniedHandler) throws Exception {
        http
                // 纯令牌认证，没有会话 cookie 可被借道，CSRF 校验关掉
                .csrf(AbstractHttpConfigurer::disable)
                // JWT 无状态，不让 Security 创建/使用 HttpSession
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 放行规则要写在 anyRequest 前面，后面的规则才轮得到
                        .requestMatchers("/auth/login", "/public/**").permitAll()
                        // 管理接口第一道防线：URL 层直接要 ADMIN 角色
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                // 未认证和越权分别返回 JSON，绝不跳登录页给前端一坨 HTML
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                // JWT 过滤器在用户名密码过滤器之前，先解析令牌填上下文
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
