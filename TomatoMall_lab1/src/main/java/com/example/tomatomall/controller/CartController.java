package com.example.tomatomall.controller;

import com.example.tomatomall.po.Cart;
import com.example.tomatomall.po.CartItem;
import com.example.tomatomall.service.CartService;
import com.example.tomatomall.util.JwtUtils;
import com.example.tomatomall.vo.CartItemVO;
import com.example.tomatomall.vo.CartRequest;
import com.example.tomatomall.vo.CartVO;
import com.example.tomatomall.vo.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    @Autowired
    private CartService cartService;

    @Autowired  // 新增注入
    private JwtUtils jwtUtils;

    @PostMapping
    public ResponseEntity<Response<CartItemVO>> addToCart(
            @RequestBody CartRequest request,
            @RequestHeader("token") String token) {
        // 从安全上下文获取用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        CartItem item = cartService.addToCart(username, request.getProductId(), request.getQuantity());
        // 转换为CartItemVO
        CartItemVO vo = new CartItemVO();
        vo.setCartItemId(item.getId());
        vo.setProductId(item.getProduct().getId());
        vo.setTitle(item.getProduct().getTitle());
        vo.setPrice(item.getProduct().getPrice());
        vo.setDescription(item.getProduct().getDescription());
        vo.setCover(item.getProduct().getCover());
        vo.setDetail(item.getProduct().getDetail());
        vo.setQuantity(item.getQuantity());

        return ResponseEntity.ok(Response.buildSuccess(vo));
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Response<String>> removeItem(
            @PathVariable Long cartItemId) {
        cartService.removeCartItem(cartItemId);
        return ResponseEntity.ok(Response.buildSuccess("删除成功"));
    }

    @PatchMapping("/{cartItemId}")
    public ResponseEntity<Response<String>> updateQuantity(
            @PathVariable Long cartItemId,
            @RequestBody Map<String, Integer> request) {
        cartService.updateQuantity(cartItemId, request.get("quantity"));
        return ResponseEntity.ok(Response.buildSuccess("修改成功"));
    }

    @GetMapping
    public ResponseEntity<Response<CartVO>> getCart(@RequestHeader("token") String token) {
        String username = jwtUtils.validateToken(token);
        CartVO cartVO = cartService.getCart(username);
        return ResponseEntity.ok(Response.buildSuccess(cartVO));
    }
}