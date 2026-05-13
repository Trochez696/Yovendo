package com.yovendo.backend.controller;

import com.yovendo.backend.dto.CreateUserRequest;
import com.yovendo.backend.dto.RoleDTO;
import com.yovendo.backend.dto.UserDTO;
import com.yovendo.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    // Servicio de administracion de usuarios y roles.
    private final UserService userService;

    // Lista todos los usuarios. Solo ADMIN tiene acceso a este modulo.
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> listUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // Consulta un usuario por id.
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> createUser(@RequestBody CreateUserRequest request) {
        // Alta de usuarios con roles definidos desde el panel de administracion.
        return ResponseEntity.ok(userService.createUser(
                request.getUsername(),
                request.getPassword(),
                request.getEmail(),
                request.getRoles()
        ));
    }

    // Actualiza datos basicos y roles del usuario.
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(
                id,
                request.getUsername(),
                request.getEmail(),
                request.getRoles()
        ));
    }

    // Activa un usuario para que pueda iniciar sesion.
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activateUser(@PathVariable Long id) {
        userService.activateUser(id);
        return ResponseEntity.ok().build();
    }

    // Desactiva un usuario sin borrarlo de la base de datos.
    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok().build();
    }

    // Borra un usuario definitivamente.
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // Devuelve los roles disponibles para llenar selects en el frontend.
    @GetMapping("/roles")
    public ResponseEntity<List<RoleDTO>> listRoles() {
        return ResponseEntity.ok(userService.getAllRoles());
    }

    // Crea un rol nuevo desde administracion.
    @PostMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoleDTO> createRole(@RequestBody RoleDTO request) {
        return ResponseEntity.ok(userService.createRole(request.getName(), request.getDescription()));
    }
}
