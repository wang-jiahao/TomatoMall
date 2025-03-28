package com.example.tomatomall.vo;

import lombok.Data;

@Data
public class UserVO {
    // 仅暴露必要字段（不包含密码）
    private String username;
    private String password;
    private String name;
    private String role;
    private String avatar;
    private String telephone;
    private String email;
    private String location;
}