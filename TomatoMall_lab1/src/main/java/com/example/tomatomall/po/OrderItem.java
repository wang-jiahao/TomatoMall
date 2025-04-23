package com.example.tomatomall.po;

import lombok.Data;
import lombok.Getter;

import javax.persistence.*;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 商品快照信息（防止商品信息变更）
    @Getter
    @Column(nullable = false, length = 100)
    private String productTitle;

    @Getter
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal productPrice;

    @Column(nullable = false)
    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "product_id", nullable = false)
    private Long productId; // 添加productId字段
}