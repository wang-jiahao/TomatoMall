package com.example.tomatomall.service;

import com.example.tomatomall.po.Product;
import com.example.tomatomall.po.Stockpile;
import com.example.tomatomall.vo.ProductVO;
import java.util.List;

public interface ProductService {
    // 获取所有商品
    List<Product> getAllProducts();

    // 获取指定商品
    Product getProductById(Long id);

    // 新增商品
    Product createProduct(ProductVO productVO);

    // 更新商品
    Product updateProduct(Long id, ProductVO productVO);

    // 删除商品
    void deleteProduct(Long id);

    // ProductService.java 新增方法
    void updateStockpile(Long productId, Integer amount);

    // ProductService.java
    Stockpile getStockpile(Long productId);
}