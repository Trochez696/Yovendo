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
// DTO de llamadas para evitar exponer directamente la entidad JPA.
public class CallDTO {
    public Long id;
    public String clientName;
    public String clientEmail;
    public String clientPhone;
    public LocalDateTime callDate;
    public String notes;
    public String callType;
    public int durationMinutes;
    public Long consultantId;
    public String consultantName;
}
