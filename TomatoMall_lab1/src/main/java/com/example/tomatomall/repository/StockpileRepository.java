package com.example.tomatomall.repository;

import com.example.tomatomall.po.Stockpile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// StockpileRepository.java 修改方法返回类型为 Optional
public interface StockpileRepository extends JpaRepository<Stockpile, Long> {
    Optional<Stockpile> findByProduct_Id(Long productId);
}