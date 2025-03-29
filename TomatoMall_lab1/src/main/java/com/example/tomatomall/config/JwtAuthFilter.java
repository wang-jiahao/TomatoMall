package com.example.tomatomall.config;

import com.example.tomatomall.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        // 放行注册和登录接口
        if (path.startsWith("/api/accounts") && (
                path.equals("/api/accounts") ||
                        path.equals("/api/accounts/login")
        ))  {
            filterChain.doFilter(request, response);
            return;
        }
        String token = request.getHeader("token");
        if (token != null) {
            try {
                String username = jwtUtils.validateToken(token);
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>())
                );
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token无效");
            }
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "缺少 Token");
            return;
        }
        filterChain.doFilter(request, response);
    }
}