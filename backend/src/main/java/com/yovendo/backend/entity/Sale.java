package com.yovendo.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "sales")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "client_name")
    private String clientName;
    
    private String clientEmail;
    
    private String clientPhone;
    
    @Column(nullable = false)
    private double amount;
    
    private String description;
    
    @Column(name = "sale_date")
    private LocalDateTime saleDate;
    
    @ManyToOne
    @JoinColumn(name = "consultant_id")
    private User consultant;
    
    @ManyToOne
    @JoinColumn(name = "client_id")
    private User client;
    
    @PrePersist
    protected void onCreate() {
        if (saleDate == null) {
            saleDate = LocalDateTime.now();
        }
    }
}
