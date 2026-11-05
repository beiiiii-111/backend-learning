package top.yixz.security.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import top.yixz.security.config.JwtProperties;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * JWT 签发与解析
 *
 * @author yixz
 * @date 2026-10-29
 * @description 令牌里只放用户名（subject），不放角色和权限点：
 * 管理员改了某人的角色，旧令牌不该继续干旧角色的事。
 * parse 时验签 + 校验签发者，篡改过的令牌直接抛 JwtException。
 */
@Service
public class JwtTokenService {

    private final SecretKey key;
    private final String issuer;
    private final long expireMinutes;

    public JwtTokenService(JwtProperties properties) {
        this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
        this.issuer = properties.getIssuer();
        this.expireMinutes = properties.getExpireMinutes();
    }

    public String issue(String username) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expireMinutes * 60_000);
        return Jwts.builder()
                .subject(username)
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key)
                .compact();
    }

    /**
     * 验签并取出用户名；无效/过期/被篡改一律抛 JwtException
     */
    public String parseSubject(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    public LocalDateTime expiresAt() {
        return LocalDateTime.ofInstant(
                Instant.now().plusSeconds(expireMinutes * 60), ZoneId.systemDefault());
    }
}
