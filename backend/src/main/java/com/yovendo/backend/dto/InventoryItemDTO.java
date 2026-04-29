package com.yovendo.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItemDTO {
    public Long id;
    public String name;
    public int quantity;
    public String description;
    public String unit;
    public double price;
    public int minStock;
    public int maxStock;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}
