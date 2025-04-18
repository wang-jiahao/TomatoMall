package com.example.tomatomall.serviceImpl;

import com.example.tomatomall.po.*;
import com.example.tomatomall.repository.*;
import com.example.tomatomall.service.CartService;
import com.example.tomatomall.service.ProductService;
import com.example.tomatomall.vo.CartItemVO;
import com.example.tomatomall.vo.CartVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CartServiceImpl implements CartService {
    private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public CartItem addToCart(String username, Long productId, Integer quantity) {
        // 获取用户购物车
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("用户不存在: {}", username);
                    return new RuntimeException("用户不存在，请检查Token有效性或用户是否注册");
                });
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        // 校验库存
        Stockpile stockpile = productService.getStockpile(productId);
        int available = stockpile.getAmount() - stockpile.getFrozen();
        if (stockpile == null) {
            throw new RuntimeException("商品库存信息不存在，productId: " + productId);
        }
        if (available < quantity) {
            throw new RuntimeException("商品库存不足，当前库存: " + available);
        }

        // 查找或创建购物车项
        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            // 更新现有数量
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            return cartItemRepository.save(existingItem);
        } else {
            // 创建新购物车项
            Product product = productService.getProductById(productId);
            CartItem newItem = new CartItem();
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            newItem.setCart(cart);
            return cartItemRepository.save(newItem);
        }
    }

    @Override
    @Transactional
    public void removeCartItem(Long cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("购物车商品不存在"));
        cartItemRepository.delete(item);
    }

    @Override
    @Transactional
    public void updateQuantity(Long cartItemId, Integer quantity) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("购物车商品不存在"));

        // 校验库存
        Stockpile stockpile = productService.getStockpile(item.getProduct().getId());
        int available = stockpile.getAmount() - stockpile.getFrozen();
        if (available < quantity) {
            throw new RuntimeException("商品库存不足，当前可用数量：" + available);
        }

        item.setQuantity(quantity);
        cartItemRepository.save(item);
    }

    @Override
    public CartVO getCart(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("用户不存在: {}", username);
                    return new RuntimeException("用户不存在，请检查Token有效性或用户是否注册");
                });
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("购物车不存在"));

        // 转换为VO
        CartVO cartVO = new CartVO();
        cartVO.setCartId(cart.getCartId());

        List<CartItemVO> items = cart.getItems().stream()
                .map(item -> {
                    CartItemVO vo = new CartItemVO();
                    // 手动映射字段
                    vo.setCartItemId(item.getId());  // 确保使用正确的ID字段
                    Product product = item.getProduct();
                    if(product != null){
                        vo.setProductId(product.getId());
                        vo.setTitle(product.getTitle());
                        vo.setPrice(product.getPrice());
                        vo.setDescription(product.getDescription());
                        vo.setCover(product.getCover());
                        vo.setDetail(product.getDetail());
                    }
                    vo.setQuantity(item.getQuantity());
                    return vo;
                }).collect(Collectors.toList());

        cartVO.setItems(items);

        // 计算总金额
        double totalAmount = items.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        cartVO.setTotalAmount(totalAmount);

        return cartVO;
    }
}