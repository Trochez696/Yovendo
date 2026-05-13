package com.yovendo.backend.service;

import com.yovendo.backend.dto.UserDTO;
import com.yovendo.backend.entity.User;
import com.yovendo.backend.repository.UserRepository;
import com.yovendo.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    // Repositorio para buscar usuarios guardados en la base de datos.
    private final UserRepository userRepository;
    // Servicio propio para crear y leer tokens JWT.
    private final JwtService jwtService;
    // Componente de Spring Security que valida usuario/contrasena.
    private final AuthenticationManager authenticationManager;
    // Carga el UserDetails que Spring Security necesita para generar permisos.
    private final UserDetailsService userDetailsService;

    // Flujo de login:
    // 1. autentica credenciales
    // 2. carga el usuario
    // 3. genera y devuelve el JWT
    public String login(String username, String password) {
        // Spring Security valida la contrasena antes de emitir el JWT.
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(username, password)
        );
        
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return jwtService.generateToken(userDetails);
    }

    // Busca el usuario por username y lo convierte a UserDTO para responder al frontend.
    public UserDTO getCurrentUser(String username) {
        // Se expone un DTO para no devolver password ni detalles internos de seguridad.
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .active(user.isEnabled())
                .roles(user.getRoles().stream().map(r -> r.getName()).toList())
                .build();
    }
}
