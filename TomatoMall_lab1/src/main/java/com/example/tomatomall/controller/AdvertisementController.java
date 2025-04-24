package com.example.tomatomall.controller;

import com.example.tomatomall.po.Advertisement;
import com.example.tomatomall.service.AdvertisementService;
import com.example.tomatomall.vo.AdvertisementVO;
import com.example.tomatomall.vo.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/advertisements")
@RequiredArgsConstructor
public class AdvertisementController {
    private final AdvertisementService advertisementService;

    @GetMapping
    public ResponseEntity<Response<List<Advertisement>>> getAllAds() {
        return ResponseEntity.ok(Response.buildSuccess(advertisementService.getAllAdvertisements()));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Response<String>> updateAd(@RequestBody AdvertisementVO vo) {
        try {
            advertisementService.updateAdvertisement(vo);
            return ResponseEntity.ok(Response.buildSuccess("更新成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Response.buildFailure("400", e.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Response<Advertisement>> createAd(@RequestBody AdvertisementVO vo) {
        Advertisement ad = advertisementService.createAdvertisement(vo);
        return ResponseEntity.ok(Response.buildSuccess(ad));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<Response<String>> deleteAd(@PathVariable Long id) {
        advertisementService.deleteAdvertisement(id);
        return ResponseEntity.ok(Response.buildSuccess("删除成功"));
    }
}
