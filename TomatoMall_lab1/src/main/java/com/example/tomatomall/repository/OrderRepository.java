package com.example.tomatomall.repository;

import com.example.tomatomall.po.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Date;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // 根据状态和过期时间查询未支付的订单
    List<Order> findByStatusAndExpireTimeBefore(String status, Date expireTime);
}