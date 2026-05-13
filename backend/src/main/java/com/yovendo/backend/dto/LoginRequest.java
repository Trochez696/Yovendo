package com.yovendo.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
// Credenciales recibidas en /api/auth/login.
public class LoginRequest {
    private String username;
    private String password;
}
