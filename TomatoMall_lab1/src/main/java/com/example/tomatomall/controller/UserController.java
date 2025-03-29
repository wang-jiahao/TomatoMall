package com.example.tomatomall.controller;

import com.example.tomatomall.po.User;
import com.example.tomatomall.service.UserService;
import com.example.tomatomall.util.JwtUtils;
import com.example.tomatomall.vo.LoginVO;
import com.example.tomatomall.vo.Response;
import com.example.tomatomall.vo.UserVO;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class UserController {

    private final UserService userService;
    private final JwtUtils jwtUtils;

    // 构造器注入（推荐方式）
    @Autowired
    public UserController(UserService userService, JwtUtils jwtUtils) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    // 用户注册
    @PostMapping
    public ResponseEntity<?> register(@RequestBody UserVO userVO) {
        try {
            userService.register(userVO);
            // 使用 Response.buildSuccess 包装成功响应
            return ResponseEntity.ok(Response.buildSuccess("注册成功"));
        } catch (RuntimeException e) {
            // 注册失败时的响应
            return ResponseEntity.badRequest()
                    .body(Response.buildFailure(e.getMessage(), "400"));
        }
    }

    // 用户登录
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginVO loginVO) {
        try {
            User user = userService.authenticate(loginVO.getUsername(), loginVO.getPassword());
            String token = jwtUtils.generateToken(user.getUsername(), user.getRole()); // 传入用户名和角色
            return ResponseEntity.ok()
                    .header("Authorization", "Bearer " + token)
                    .body(Response.buildSuccess(token));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Response.buildFailure(e.getMessage(), "401"));
        }
    }

    // 获取用户详情
    // UserController.java
    @GetMapping("/{username}")
    public ResponseEntity<?> getUser(
            @PathVariable String username,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Claims claims = jwtUtils.validateToken(token); // 返回 Claims 对象
            String currentUser = claims.getSubject();      // 从 Claims 中提取用户名

            // 调试日志
            System.out.println("Token 中的用户名: " + currentUser);
            System.out.println("URL 中的用户名: " + username);

            if (!currentUser.trim().equalsIgnoreCase(username.trim())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Response.buildFailure("无权限访问", "403"));
            }

            UserVO userVO = userService.getUserVOByUsername(username);
            return ResponseEntity.ok(Response.buildSuccess(userVO));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Response.buildFailure(e.getMessage(), "401"));
        }
    }

    // 更新用户信息
    @PutMapping
    public ResponseEntity<?> updateUser(
            @RequestBody UserVO userVO,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Claims claims = jwtUtils.validateToken(token); // 返回 Claims 对象
            String username = claims.getSubject();        // 从 Claims 中提取用户名

            if (userVO.getUsername() != null && !userVO.getUsername().equals(username)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Response.buildFailure("无权修改用户名", "403"));
            }

            userService.updateUser(username, userVO);
            return ResponseEntity.ok(Response.buildSuccess("更新成功"));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Response.buildFailure(e.getMessage(), "401"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.buildFailure("服务器内部错误", "500"));
        }
    }
}