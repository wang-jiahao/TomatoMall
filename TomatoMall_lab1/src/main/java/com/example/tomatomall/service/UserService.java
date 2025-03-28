package com.example.tomatomall.service;

import com.example.tomatomall.po.User;
import com.example.tomatomall.vo.UserVO;

public interface UserService {
    // 用户注册（VO → PO）
    User register(UserVO userVO);

    // 用户登录认证
    User authenticate(String username, String password);

    // 根据用户名获取用户信息（PO → VO）
    UserVO getUserVOByUsername(String username);

    // 更新用户信息
    User updateUser(String username, UserVO userVO);
}
