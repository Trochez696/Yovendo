package com.yovendo.backend.controller;

import com.yovendo.backend.dto.CallDTO;
import com.yovendo.backend.entity.User;
import com.yovendo.backend.repository.UserRepository;
import com.yovendo.backend.service.CallService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/calls")
@RequiredArgsConstructor
public class CallsController {

    // CallService contiene la logica de negocio; UserRepository identifica al usuario logueado.
    private final CallService callService;
    private final UserRepository userRepository;

    // GET /api/calls
    // ADMIN y DIRECTOR ven todas las llamadas; CONSULTOR ve solo las suyas.
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'CONSULTOR')")
    public ResponseEntity<List<CallDTO>> listCalls(@AuthenticationPrincipal UserDetails userDetails) {
        // Consultores ven sus llamadas; administradores/directores ven el consolidado.
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        return ResponseEntity.ok(callService.getCallsForUser(user));
    }

    // POST /api/calls
    // Solo CONSULTOR puede registrar una llamada nueva.
    @PostMapping
    @PreAuthorize("hasRole('CONSULTOR')")
    public ResponseEntity<CallDTO> createCall(@RequestBody CallDTO request, @AuthenticationPrincipal UserDetails userDetails) {
        // Cada llamada queda asociada al consultor que esta autenticado.
        return ResponseEntity.ok(callService.createCall(request, userDetails.getUsername()));
    }
}
