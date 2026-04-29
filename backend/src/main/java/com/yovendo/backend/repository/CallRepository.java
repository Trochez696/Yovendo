package com.yovendo.backend.repository;

import com.yovendo.backend.entity.Call;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CallRepository extends JpaRepository<Call, Long> {
    List<Call> findByConsultantIdOrderByCallDateDesc(Long consultantId);
    List<Call> findByCallDateBetween(LocalDateTime start, LocalDateTime end);
}
