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
public class SaleDTO {
    public Long id;
    public String clientName;
    public String clientEmail;
    public String clientPhone;
    public double amount;
    public String description;
    public LocalDateTime saleDate;
    public Long consultantId;
    public String consultantName;
}
