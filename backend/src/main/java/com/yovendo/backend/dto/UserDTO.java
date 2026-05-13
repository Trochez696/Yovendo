package com.yovendo.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// Vista publica del usuario: no incluye contrasena ni datos sensibles.
public class UserDTO {
    public Long id;
    public String username;
    public String email;
    public boolean active;
    public List<String> roles;
}
