package com.yovendo.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String description;
    // Clasifica la actividad: seguimiento, venta, control, etc.
    private String type;
    private LocalDateTime date;
    @ManyToOne
    private User user;
    // Pendiente de implementar getters y setters si esta entidad se usa directamente.
}
