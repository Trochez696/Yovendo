package com.yovendo.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "calls")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Call {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "client_name")
    private String clientName;
    
    private String clientEmail;
    
    private String clientPhone;
    
    @Column(name = "call_date")
    private LocalDateTime callDate;
    
    @Column(length = 1000)
    private String notes;
    
    private String callType;
    
    private int durationMinutes;
    
    @ManyToOne
    @JoinColumn(name = "consultant_id")
    private User consultant;
    
    @ManyToOne
    @JoinColumn(name = "client_id")
    private User client;
    
    @PrePersist
    protected void onCreate() {
        if (callDate == null) {
            callDate = LocalDateTime.now();
        }
    }
}
