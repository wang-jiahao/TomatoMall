package com.example.tomatomall.vo;

import lombok.Data;
import java.util.List;

@Data
public class CartVO {
    private Long cartId;
    private List<CartItemVO> items;
    private Integer totalItems;
    private Double totalAmount;

    public Integer getTotalItems() {
        return items != null ? items.size() : 0;
    }
}
