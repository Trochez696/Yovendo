package com.yovendo.backend.repository;

import com.yovendo.backend.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    // Consultas derivadas para reportes por consultor, cliente y rango de fechas.
    List<Sale> findByConsultantIdOrderBySaleDateDesc(Long consultantId);
    List<Sale> findByClientIdOrderBySaleDateDesc(Long clientId);
    List<Sale> findBySaleDateBetween(LocalDateTime start, LocalDateTime end);
    List<Sale> findByConsultantIdAndSaleDateBetween(Long consultantId, LocalDateTime start, LocalDateTime end);
}
