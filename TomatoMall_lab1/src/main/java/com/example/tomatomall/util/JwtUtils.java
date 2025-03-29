package com.example.tomatomall.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component // 关键注解，确保被 Spring 管理
public class JwtUtils {
    private static final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private static final long EXPIRATION_TIME = 86400000; // Token 有效期 24 小时

    // 生成 Token（基于用户名）
    public String generateToken(String username, String role) {
    return Jwts.builder()
            .setSubject(username)
            .claim("role", role) // 添加角色声明
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
            .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
            .compact();
}

    // 验证 Token 并返回用户名
    public Claims validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("Token 已过期");
        } catch (MalformedJwtException e) {
            throw new RuntimeException("Token 格式错误");
        } catch (Exception e) {
            throw new RuntimeException("Token 无效");
        }
    }
}