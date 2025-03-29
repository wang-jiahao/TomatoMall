package com.example.tomatomall.controller;

import com.example.tomatomall.po.User;
import com.example.tomatomall.service.UserService;
import com.example.tomatomall.util.JwtUtils;
import com.example.tomatomall.vo.LoginVO;
import com.example.tomatomall.vo.Response;
import com.example.tomatomall.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/accounts")
public class UserController {
//    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

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
    public ResponseEntity<Response<String>> register(@RequestBody UserVO userVO) {
        try {
            userService.register(userVO);
            return ResponseEntity.ok(Response.buildSuccess("注册成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Response.buildFailure(e.getMessage(),"400"));
        }
    }

    // 用户登录
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginVO loginVO) {
        try {
            User user = userService.authenticate(loginVO.getUsername(), loginVO.getPassword());
//            System.out.println("[Debug] Generating token for: " + user.getUsername());
            String token = jwtUtils.generateToken(user.getUsername());
//            System.out.println("[Debug] Generated token: " + token);
            return ResponseEntity.ok()
                    .header("token", token) // 在响应头中添加 Token
                    .body(Response.buildSuccess(token));
        } catch (RuntimeException e) {
//            System.out.println("[Error] Login failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Response.buildFailure(e.getMessage(),"401"));
        }
    }

    // 获取用户详情
    @GetMapping("/{username}")
    public ResponseEntity<Response<?>> getUser(
            @PathVariable String username,
            @RequestHeader("token") String token
    ) {
        try {
            String currentUser = jwtUtils.validateToken(token);
//            logger.debug("Token解析的用户名: {}", currentUser);
//            logger.debug("请求路径的用户名: {}", username);

            if (!currentUser.equals(username)) {
//                logger.error("权限校验失败: Token用户={}, 请求用户={}", currentUser, username);
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
            @RequestBody UserVO userVO,  // 确保请求体存在且为JSON格式
            @RequestHeader("token") String token
    ) {
        try {
            String username = jwtUtils.validateToken(token);
            userService.updateUser(username, userVO);
            return ResponseEntity.ok(Response.buildSuccess("更新成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Response.buildFailure(e.getMessage(), "400"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.buildFailure("服务器内部错误", "500"));
        }
    }
}