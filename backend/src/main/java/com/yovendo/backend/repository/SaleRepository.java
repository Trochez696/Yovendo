package com.yovendo.backend.repository;

import com.yovendo.backend.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findByConsultantIdOrderBySaleDateDesc(Long consultantId);
    List<Sale> findByClientIdOrderBySaleDateDesc(Long clientId);
    List<Sale> findBySaleDateBetween(LocalDateTime start, LocalDateTime end);
    List<Sale> findByConsultantIdAndSaleDateBetween(Long consultantId, LocalDateTime start, LocalDateTime end);
}