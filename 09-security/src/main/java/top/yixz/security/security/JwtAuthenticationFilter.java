package top.yixz.security.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import top.yixz.security.service.AccountUserDetailsService;

import java.io.IOException;

/**
 * JWT 认证过滤器
 *
 * @author yixz
 * @date 2026-10-29
 * @description 只解析令牌、只填上下文，放不放行交给过滤链的 authorizeHttpRequests。
 * 任何解析失败（篡改、过期、账号被删）都静默清掉上下文，让请求以"未认证"身份
 * 继续，最后由 RestAuthenticationEntryPoint 统一返回 401 JSON，这里绝不直接写响应。
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenService tokenService;
    private final AccountUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenService tokenService,
                                   AccountUserDetailsService userDetailsService) {
        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String username = tokenService.parseSubject(header.substring(BEARER_PREFIX.length()));
                SecurityUser user = userDetailsService.loadUserByUsername(username);
                if (!user.isEnabled()) {
                    // 账号被停用：已签发的令牌也活不过下一次请求，直接按未认证处理
                    SecurityContextHolder.clearContext();
                } else {
                    var authentication = new UsernamePasswordAuthenticationToken(
                            user, null, user.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (JwtException | AuthenticationException e) {
                // UsernameNotFoundException 是 AuthenticationException 的子类，
                // 账号被删时也统一按未认证处理
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}
