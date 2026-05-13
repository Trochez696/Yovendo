package com.yovendo.backend.dto;

import java.time.LocalDateTime;

// Datos que representan una actividad al intercambiar informacion con la API.
public class ActivityDTO {
    public Long id;
    public String description;
    public String type;
    public LocalDateTime date;
    public Long userId;
}
