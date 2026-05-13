package com.yovendo.backend.controller;

import com.yovendo.backend.dto.LoginRequest;
import com.yovendo.backend.dto.UserDTO;
import com.yovendo.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    // Servicio que concentra la logica de autenticacion y consulta del usuario actual.
    private final AuthService authService;

    // POST /api/auth/login
    // Recibe usuario y contrasena, valida las credenciales y devuelve un token JWT.
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        // Valida credenciales y devuelve el token que usara el frontend.
        String token = authService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(Map.of("token", token));
    }

    // GET /api/auth/me
    // Usa el usuario autenticado por Spring Security para devolver sus datos publicos.
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getMe(@AuthenticationPrincipal UserDetails userDetails) {
        // Devuelve el usuario autenticado a partir del token enviado.
        UserDTO user = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(user);
    }
}
