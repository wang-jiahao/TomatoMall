package com.example.tomatomall.serviceImpl;

import com.example.tomatomall.po.Advertisement;
import com.example.tomatomall.po.Product;
import com.example.tomatomall.repository.AdvertisementRepository;
import com.example.tomatomall.repository.ProductRepository;
import com.example.tomatomall.service.AdvertisementService;
import com.example.tomatomall.vo.AdvertisementVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor// 有效替代多个Autowired
public class AdvertisementServiceImpl implements AdvertisementService {
    private final AdvertisementRepository advertisementRepository;//1
    private final ProductRepository productRepository;//2

    @Override
    public List<Advertisement> getAllAdvertisements() {
        return advertisementRepository.findAll();
    }

    @Override
    @Transactional
    public void updateAdvertisement(AdvertisementVO vo) {
        Advertisement ad = advertisementRepository.findById(vo.getId())
                .orElseThrow(() -> new RuntimeException("广告不存在"));

        // 校验商品ID是否存在
        if(vo.getProductId() != null && !vo.getProductId().equals(ad.getProduct().getId())) {
            boolean productExists = productRepository.existsById(vo.getProductId());
            if(!productExists) {
                throw new RuntimeException("商品不存在");
            }
            Product newProduct = new Product();
            newProduct.setId(vo.getProductId());
            ad.setProduct(newProduct);
        }

        // 更新其他字段
        if(vo.getTitle() != null) ad.setTitle(vo.getTitle());
        if(vo.getContent() != null) ad.setContent(vo.getContent());
        if(vo.getImgUrl() != null) ad.setImgUrl(vo.getImgUrl());

        advertisementRepository.save(ad);
    }

    @Override
    @Transactional
    public Advertisement createAdvertisement(AdvertisementVO vo) {
        Product product = productRepository.findById(vo.getProductId())
                .orElseThrow(() -> new RuntimeException("商品不存在"));

        Advertisement ad = new Advertisement();
        BeanUtils.copyProperties(vo, ad);
        ad.setProduct(product);

        return advertisementRepository.save(ad);
    }

    // 使用实体直接转换（替代VO类）
    private Map<String, Object> convertToResponse(Advertisement ad) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", ad.getId());
        response.put("title", ad.getTitle());
        response.put("content", ad.getContent());
        response.put("imgUrl", ad.getImgUrl());
        response.put("productId", ad.getProductId());
        return response;
    }

    @Override
    @Transactional
    public void deleteAdvertisement(Long id) {
        advertisementRepository.deleteById(id);
    }
}
