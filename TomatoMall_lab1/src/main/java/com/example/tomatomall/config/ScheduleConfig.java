package com.example.tomatomall.config;

import com.example.tomatomall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class ScheduleConfig {
    
    @Autowired
    private OrderService orderService;
    
    // 每5分钟检查一次超时订单
    @Scheduled(fixedRate = 300_000)
    public void checkOrderExpiration() {
        orderService.cancelExpiredOrders();
    }
}