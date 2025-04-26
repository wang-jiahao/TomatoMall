package com.example.tomatomall.vo;

import lombok.Data;

@Data
public class AdvertisementVO {
    private Long id;
    private String title;
    private String content;
    private String imgUrl;
    private Long productId;
}
