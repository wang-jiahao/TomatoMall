package com.example.tomatomall.controller;

import com.example.tomatomall.po.Product;
import com.example.tomatomall.po.Stockpile;
import com.example.tomatomall.service.ProductService;
import com.example.tomatomall.vo.ProductVO;
import com.example.tomatomall.vo.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    @Autowired
    private ProductService productService;

    // 获取商品列表（允许匿名访问）
    @GetMapping
    public ResponseEntity<Response<List<Product>>> getAllProducts() {
        return ResponseEntity.ok(Response.buildSuccess(productService.getAllProducts()));
    }

    // 获取单个商品（允许匿名访问）
    @GetMapping("/{id}")
    public ResponseEntity<Response<Product>> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(Response.buildSuccess(productService.getProductById(id)));
    }

    // 新增商品（仅管理员）
    @PostMapping
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Response<Product>> createProduct(@RequestBody ProductVO productVO) {
        return ResponseEntity.ok(Response.buildSuccess(productService.createProduct(productVO)));
    }

    // 更新商品（仅管理员）
    @PutMapping
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Response<Product>> updateProduct(@RequestBody ProductVO productVO) {
        return ResponseEntity.ok(Response.buildSuccess(productService.updateProduct(productVO.getId(), productVO)));
    }

    // 删除商品（仅管理员）
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Response<String>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(Response.buildSuccess("删除成功"));
    }

    // 调整商品库存（管理员权限）
    @PatchMapping("/stockpile/{productId}")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Response<String>> updateStockpile(
            @PathVariable Long productId,
            @RequestBody Map<String, Integer> request) {
        Integer amount = request.get("amount");
        productService.updateStockpile(productId, amount);
        return ResponseEntity.ok(Response.buildSuccess("调整库存成功"));
    }

    @GetMapping("/stockpile/{productId}")
    public ResponseEntity<Response<Stockpile>> getStockpile(
            @PathVariable Long productId) {
        Stockpile stockpile = productService.getStockpile(productId);
        return ResponseEntity.ok(Response.buildSuccess(stockpile));
    }
}