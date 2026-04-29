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
public class NotificationDTO {
    public Long id;
    public String message;
    public String recipientRole;
    public Long recipientId;
    public boolean read;
    public String type;
    public LocalDateTime createdAt;
}
