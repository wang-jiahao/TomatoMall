package com.example.tomatomall.vo;

import lombok.Data;

import java.util.List;

@Data
public class CheckoutRequest {
    private List<Long> cartItemIds;

    private String shippingAddress;

    private String paymentMethod;

    // 新增收货地址内部类
    @Data
    public static class ShippingAddress {
        private String name;

        private String phone;

        private String address;
    }
}