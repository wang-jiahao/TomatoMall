package com.example.tomatomall.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "stockpile")
public class Stockpile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private Product product;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false)
    private Integer frozen;

    // 新增以下字段用于序列化
    @Transient // 表示该字段不持久化到数据库
    private Long productId;

    @PostLoad // 在实体加载后自动填充 productId
    private void postLoad() {
        this.productId = (product != null) ? product.getId() : null;
    }
}