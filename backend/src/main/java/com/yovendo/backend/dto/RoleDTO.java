package com.yovendo.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// DTO simple para consultar y crear roles.
public class RoleDTO {
    public Long id;
    public String name;
    public String description;
}
