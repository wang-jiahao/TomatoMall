package com.example.tomatomall.vo;

import lombok.Data;
import java.util.Set;

@Data
public class ProductVO {
    private Long id;
    // 必填字段
    private String title;
    private Double price;
    private Double rate;
    // 可选字段
    private String description;
    private String cover;
    private String detail;
    // 规格集合
    private Set<SpecificationVO> specifications;
}