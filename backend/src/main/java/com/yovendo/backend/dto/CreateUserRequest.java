package com.yovendo.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
// Cuerpo esperado para crear o actualizar usuarios desde administracion.
public class CreateUserRequest {
    private String username;
    private String password;
    private String email;
    private List<String> roles;
}
