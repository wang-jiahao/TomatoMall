package com.example.tomatomall.po;

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
    private Product product;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false)
    private Integer frozen;
}