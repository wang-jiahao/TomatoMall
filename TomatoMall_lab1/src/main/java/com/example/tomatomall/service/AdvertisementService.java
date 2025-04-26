package com.example.tomatomall.service;

import com.example.tomatomall.po.Advertisement;
import com.example.tomatomall.vo.AdvertisementVO;
import java.util.List;

public interface AdvertisementService {
    List<Advertisement> getAllAdvertisements();
    void updateAdvertisement(AdvertisementVO vo);
    Advertisement createAdvertisement(AdvertisementVO vo);
    void deleteAdvertisement(Long id);
}