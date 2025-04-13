package com.example.tomatomall.po;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "products")
@EqualsAndHashCode(exclude = "specifications") // 排除关联字段
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(nullable = false, precision = 10, scale = 2)
    private Double price;

    @Column(nullable = false)
    private Double rate;

    @Column(length = 255)
    private String description;

    @Column(length = 500)
    private String cover;

    @Column(length = 500)
    private String detail;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Specification> specifications = new HashSet<>(); //初始化为空集合

    // 手动重写 toString()，排除关联字段
    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", price=" + price +
                // 仅包含基本字段，不包含 specifications
                '}';
    }
}