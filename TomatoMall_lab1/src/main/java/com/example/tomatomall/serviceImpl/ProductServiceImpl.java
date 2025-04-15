package com.example.tomatomall.serviceImpl;

import com.example.tomatomall.po.Product;
import com.example.tomatomall.po.Specification;
import com.example.tomatomall.po.Stockpile;
import com.example.tomatomall.repository.ProductRepository;
import com.example.tomatomall.repository.StockpileRepository;
import com.example.tomatomall.service.ProductService;
import com.example.tomatomall.vo.ProductVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import java.util.HashSet;
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
        try {
            Product product = new Product();
            BeanUtils.copyProperties(productVO, product);
            product.setSpecifications(new HashSet<>());

            productVO.getSpecifications().forEach(specVO -> {
                Specification spec = new Specification();
                BeanUtils.copyProperties(specVO, spec);
                spec.setProduct(product);
                product.getSpecifications().add(spec);

                // 仅打印规格名称和值
                System.out.println("[DEBUG] 规格: " + spec.getItem() + " - " + spec.getValue());
            });

            Product savedProduct = productRepository.saveAndFlush(product);
            System.out.println("[DEBUG] 商品保存成功，ID: " + savedProduct.getId());
            return savedProduct;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("创建商品失败: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public Product updateProduct(Long id, ProductVO productVO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商品不存在"));

        // 手动复制非空字段（避免覆盖数据库原有值）
        if (productVO.getTitle() != null) {
            product.setTitle(productVO.getTitle());
        }
        if (productVO.getPrice() != null) {
            if (productVO.getPrice() < 0) {
                throw new RuntimeException("价格不能为负数");
            }
            product.setPrice(productVO.getPrice());
        }
        if (productVO.getRate() != null) {
            if (productVO.getRate() < 0 || productVO.getRate() > 10) {
                throw new RuntimeException("评分需在0~10之间");
            }
            product.setRate(productVO.getRate());
        }
        if (productVO.getDescription() != null) {
            product.setDescription(productVO.getDescription());
        }
        if (productVO.getCover() != null) {
            product.setCover(productVO.getCover());
        }
        if (productVO.getDetail() != null) {
            product.setDetail(productVO.getDetail());
        }

        // 更新规格（需校验 productId 一致性）
        if (productVO.getSpecifications() != null) {
            productVO.getSpecifications().forEach(specVO -> {
                if (specVO.getProductId() == null || !specVO.getProductId().equals(id)) {
                    throw new RuntimeException("规格的 productId 必须与商品 ID 一致");
                }
            });

            // 先清空旧规格
            product.getSpecifications().clear();

            // 添加新规格
            productVO.getSpecifications().forEach(specVO -> {
                Specification spec = new Specification();
                spec.setItem(specVO.getItem());
                spec.setValue(specVO.getValue());
                spec.setProduct(product); // 确保关联当前商品
                product.getSpecifications().add(spec);
            });
        }

        return productRepository.save(product);
    }

    @Override
    public void deleteProduct(Long id) {

        if (!productRepository.existsById(id)) {
            throw new RuntimeException("商品不存在");
        }
        //先删除库存
        stockpileRepository.deleteById(id);
        //再删除商品
        productRepository.deleteById(id);
    }

    @Autowired
    private StockpileRepository stockpileRepository;

    @Override
    @Transactional
    public void updateStockpile(Long productId, Integer amount) {
        // 检查商品是否存在
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));

        // 查找库存记录，不存在时创建新记录（根据业务需求）
        Stockpile stockpile = stockpileRepository.findByProduct_Id(productId)
                .orElseGet(() -> {
                    Stockpile newStockpile = new Stockpile();
                    newStockpile.setProduct(product);
                    newStockpile.setAmount(0); // 默认库存为0
                    newStockpile.setFrozen(0); // 默认冻结数为0
                    return stockpileRepository.save(newStockpile);
                });

        // 校验参数
        if (amount < 0) {
            throw new RuntimeException("库存数不能为负");
        }

        // 更新库存
        stockpile.setAmount(amount);
        stockpileRepository.save(stockpile);
    }

    @Override
    public Stockpile getStockpile(Long productId) {
        // 先检查商品是否存在
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        // 再查询库存记录
        return stockpileRepository.findByProduct_Id(productId)
                .orElseThrow(() -> new RuntimeException("库存记录不存在"));
    }
}