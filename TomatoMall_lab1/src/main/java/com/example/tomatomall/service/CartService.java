package com.example.tomatomall.service;

import com.example.tomatomall.po.Cart;
import com.example.tomatomall.po.CartItem;
import com.example.tomatomall.vo.CartVO;

public interface CartService {
    CartItem addToCart(String username, Long productId, Integer quantity);
    void removeCartItem(Long cartItemId);
    void updateQuantity(Long cartItemId, Integer quantity);
    CartVO getCart(String username);
}
