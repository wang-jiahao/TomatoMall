package com.example.tomatomall.vo;

import lombok.Data;

@Data
public class CartItemVO {
    private Long cartItemId;
    private Long productId;  // 直接存储productId而不是整个product对象
    private String title;
    private Double price;
    private String description;
    private String cover;
    private String detail;
    private Integer quantity;
}
