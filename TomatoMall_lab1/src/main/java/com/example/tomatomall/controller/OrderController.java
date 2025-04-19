package com.example.tomatomall.controller;

import com.example.tomatomall.po.Order;
import com.example.tomatomall.service.OrderService;
import com.example.tomatomall.util.JwtUtils;
import com.example.tomatomall.vo.CheckoutRequest;
import com.example.tomatomall.vo.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private JwtUtils jwtUtils;

    // 提交订单（返回自定义数据结构）
    @PostMapping("/api/cart/checkout")
    public ResponseEntity<Response<Map<String, Object>>> checkout(
            @RequestBody CheckoutRequest request,
            @RequestHeader("token") String token) {
        String username = jwtUtils.validateToken(token);
        Order order = orderService.createOrder(
            username,
            request.getCartItemIds(),
            request.getShippingAddress(),
            request.getPaymentMethod()
        );

        // 构建符合需求的返回数据
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("orderId", order.getOrderId());
        responseData.put("username", order.getUser().getUsername());
        responseData.put("totalAmount", order.getTotalAmount());
        responseData.put("paymentMethod", order.getPaymentMethod());
        responseData.put("createTime", order.getCreateTime());
        responseData.put("status", order.getStatus());

        return ResponseEntity.ok(Response.buildSuccess(responseData));
    }

    // 发起支付（返回完整支付信息）
    @PostMapping("/api/orders/{orderId}/pay")
    public ResponseEntity<Response<Map<String, Object>>> payOrder(@PathVariable Long orderId) {
        String paymentForm = orderService.generatePaymentForm(orderId);
        Order order = orderService.getOrderById(orderId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("paymentForm", paymentForm);
        responseData.put("orderId", order.getOrderId());
        responseData.put("totalAmount", order.getTotalAmount());
        responseData.put("paymentMethod", order.getPaymentMethod());

        return ResponseEntity.ok(Response.buildSuccess(responseData));
    }
}