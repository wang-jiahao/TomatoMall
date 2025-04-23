package com.example.tomatomall.service;

import com.example.tomatomall.po.Order;

import java.util.List;

public interface OrderService {
    Order createOrder(String username, List<Long> cartItemIds, String shippingAddress, String paymentMethod);
    String generatePaymentForm(Long orderId);
    void handlePaymentSuccess(Long orderId, String alipayTradeNo, String amount);
    void cancelExpiredOrders();
    Order getOrderById(Long orderId);
}