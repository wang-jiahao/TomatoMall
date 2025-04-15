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
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class JwtUtils {
//    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class); // 添加此行
    private static final long EXPIRATION_TIME = 86400000; // 24小时

    @Autowired
    private UserRepository userRepository;

    // 生成 Token（基于用户名，使用用户密码作为动态密钥）
    public String generateToken(String username) {
        // 从 Optional<User> 中提取用户对象，若不存在则抛出异常
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                    new RuntimeException("用户不存在"));

        // 关键修改：对密码进行Base64编码
        String secretKey = Base64.getEncoder().encodeToString(user.getPassword().getBytes());
        return Jwts.builder()
                .setSubject(username)
                .claim("role", user.getRole())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    // 验证 Token 并返回用户名
    public String validateToken(String token) {
        try {
            // 先解析Token头获取用户名（不验证签名）
            Claims unsignedClaims = Jwts.parser()
                    .parseClaimsJwt(token.substring(0, token.lastIndexOf('.') + 1)) // 仅解析头部和载荷
                    .getBody();
            String username = unsignedClaims.getSubject();
//            logger.debug("[JwtUtils] 初步解析用户名: {}", username);


            // 从 Optional<User> 中提取用户对象，若不存在则抛出异常
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
//            logger.debug("[JwtUtils] 数据库中的用户密码: {}", user.getPassword());


            // 关键修改：使用相同的Base64编码逻辑
            String secretKey = Base64.getEncoder().encodeToString(user.getPassword().getBytes());
//            logger.debug("[JwtUtils] 生成的密钥: {}", secretKey);
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
//            System.out.println("[Debug] SecretKey for validation: " + secretKey);
            return claims.getSubject();
        } catch (Exception e) {
//            logger.error("[JwtUtils] Token验证异常: {}", e.getMessage());
            throw new RuntimeException("Token 无效或已过期: " + e.getMessage());
        }
    }

    // 解析Token中的Claims用于获取角色
    public Claims parseTokenClaims(String token) {
        try {
            String username = validateToken(token); // 先验证Token有效性
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            String secretKey = Base64.getEncoder().encodeToString(user.getPassword().getBytes());
            return Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            throw new RuntimeException("Token解析失败");
        }
    }
}