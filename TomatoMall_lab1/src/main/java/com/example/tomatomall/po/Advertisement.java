package com.example.tomatomall.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "advertisements")
public class Advertisement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imgUrl;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore // 添加此注解，忽略product字段的序列化
    private Product product;

    @Transient // 添加瞬态字段用于VO转换
    private Long productId;

    public Long getProductId() {
        return this.product != null ? this.product.getId() : null;
    }
}