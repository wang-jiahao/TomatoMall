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
            String token = jwtUtils.generateToken(user.getUsername());
            return ResponseEntity.ok()
                    .header("token", token) // 在响应头中添加 Token
                    .body(Response.buildSuccess(token));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Response.buildFailure("用户不存在/用户密码错误","401"));
        }
    }

    // 获取用户详情
    @GetMapping("/{username}")
    public ResponseEntity<?> getUser(
            @PathVariable String username,
            @RequestHeader("token") String token
    ) {
        try {
            String currentUser = jwtUtils.validateToken(token);
            if (!currentUser.equals(username)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("无权限访问");
            }
            UserVO userVO = userService.getUserVOByUsername(username);
            return ResponseEntity.ok(userVO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    // 更新用户信息
    @PutMapping
    public ResponseEntity<?> updateUser(
            @RequestBody UserVO userVO,
            @RequestHeader("token") String token
    ) {
        try {
            String username = jwtUtils.validateToken(token);
            userService.updateUser(username, userVO);
            return ResponseEntity.ok("更新成功");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}