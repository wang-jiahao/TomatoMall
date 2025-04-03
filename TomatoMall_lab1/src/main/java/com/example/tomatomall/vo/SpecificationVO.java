package com.example.tomatomall.vo;

import lombok.Data;

@Data
public class SpecificationVO {
    private Long id;
    private String item;    // 规格名称（如"作者"）
    private String value;   // 规格内容（如"周志明"）
    private Long productId; // 关联的商品ID
}