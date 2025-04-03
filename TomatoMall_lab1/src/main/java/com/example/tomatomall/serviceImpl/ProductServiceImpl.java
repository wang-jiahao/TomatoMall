package com.example.tomatomall.serviceImpl;

import com.example.tomatomall.po.Product;
import com.example.tomatomall.po.Specification;
import com.example.tomatomall.repository.ProductRepository;
import com.example.tomatomall.service.ProductService;
import com.example.tomatomall.vo.ProductVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
    }

    @Transactional
    @Override
    public Product createProduct(ProductVO productVO) {
        Product product = new Product();
        BeanUtils.copyProperties(productVO, product);

        // 处理规格
        productVO.getSpecifications().forEach(specVO -> {
            Specification spec = new Specification();
            BeanUtils.copyProperties(specVO, spec);
            spec.setProduct(product); // 关联实体
            product.getSpecifications().add(spec);
        });

        return productRepository.save(product);
    }

    @Transactional
    @Override
    public Product updateProduct(Long id, ProductVO productVO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        BeanUtils.copyProperties(productVO, product, "id");
        // 更新规格（先删除旧规格）
        product.getSpecifications().clear();
        productVO.getSpecifications().forEach(specVO -> {
            Specification spec = new Specification();
            BeanUtils.copyProperties(specVO, spec);
            spec.setProduct(product);
            product.getSpecifications().add(spec);
        });
        return productRepository.save(product);
    }

    @Override
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}