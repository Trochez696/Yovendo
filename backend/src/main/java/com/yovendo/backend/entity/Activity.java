package com.yovendo.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String description;
    private String type; // seguimiento, venta, control, etc.
    private LocalDateTime date;
    @ManyToOne
    private User user;
    // Getters y setters
}
