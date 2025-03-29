package com.example.tomatomall.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import com.example.tomatomall.po.User;
import com.example.tomatomall.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtUtils {
    private static final long EXPIRATION_TIME = 86400000; // 24小时

    @Autowired
    private UserRepository userRepository;

    // 生成 Token（基于用户名，使用用户密码作为动态密钥）
    public String generateToken(String username) {
        // 从 Optional<User> 中提取用户对象，若不存在则抛出异常
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, user.getPassword())
                .compact();
    }

    // 验证 Token 并返回用户名
    public String validateToken(String token) {
        try {
            // 先解析 Token 头获取用户名（无需密钥）
            String username = Jwts.parser()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();

            // 从 Optional<User> 中提取用户对象，若不存在则抛出异常
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 使用用户密码重新验证 Token
            Claims claims = Jwts.parser()
                    .setSigningKey(user.getPassword())
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getSubject();
        } catch (Exception e) {
            throw new RuntimeException("Token 无效或已过期: " + e.getMessage());
        }
    }
}