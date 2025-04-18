package com.example.tomatomall.vo;

import lombok.Data;

@Data
public class CartRequest {
    private Long productId;
    private Integer quantity;
}