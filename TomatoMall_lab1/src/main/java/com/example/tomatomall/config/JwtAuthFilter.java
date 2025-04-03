package com.example.tomatomall.config;

import com.example.tomatomall.util.JwtUtils;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
//    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);
    @Autowired
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
//        logger.debug("[JwtAuthFilter] 请求路径: {}", path);

        // 放行注册和登录接口
        if (path.equals("/api/accounts") || path.equals("/api/accounts/login")) {
//            logger.debug("[JwtAuthFilter] 放行路径: {}", path);
            filterChain.doFilter(request, response);
            return;
        }
        String token = request.getHeader("token");
//        logger.debug("[JwtAuthFilter] 接收到的Token: {}", token);
        if (token == null) {
//            logger.error("[JwtAuthFilter] Token为空");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "缺少 Token");
            return; // 关键：发送错误后立即返回
        }
        try {
            String username = jwtUtils.validateToken(token);
            Claims claims = jwtUtils.parseTokenClaims(token); // 需要新增解析方法

            String role = "ROLE_" + claims.get("role", String.class);
            List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority(role)
            );
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(username, null, authorities)
            );
        } catch (Exception e) {
//            logger.error("[JwtAuthFilter] Token验证失败: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token无效");
            return; // 关键：发送错误后立即返回
        }
        filterChain.doFilter(request, response);
    }
}