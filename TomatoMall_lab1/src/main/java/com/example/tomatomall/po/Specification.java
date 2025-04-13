package com.example.tomatomall.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Data
@Entity
@Table(name = "specifications")
@EqualsAndHashCode(exclude = "product") // 排除关联字段
public class Specification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String item;

    @Column(nullable = false, length = 255)
    private String value;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore // 防止 JSON 序列化循环引用
    private Product product;

    @Transient // 不持久化到数据库
    private Long productId;

    // 在实体加载后自动填充 productId
    @PostLoad
    private void postLoad() {
        this.productId = (product != null) ? product.getId() : null;
    }

    // 手动重写 toString()，排除关联字段
    @Override
    public String toString() {
        return "Specification{" +
                "id=" + id +
                ", item='" + item + '\'' +
                ", value='" + value + '\'' +
                // 不包含 product 字段
                '}';
    }
}