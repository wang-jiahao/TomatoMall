package com.example.tomatomall.repository;

import com.example.tomatomall.po.Cart;
import com.example.tomatomall.po.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    // 新增两个查询方法
    Optional<Cart> findByUser(User user);
    Cart findByUser_Username(String username);
}