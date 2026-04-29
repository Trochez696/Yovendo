package com.yovendo.backend.repository;

import com.yovendo.backend.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryItem, Long> {
    List<InventoryItem> findByQuantityLessThanEqual(int minStock);
    List<InventoryItem> findByNameContainingIgnoreCase(String name);
}