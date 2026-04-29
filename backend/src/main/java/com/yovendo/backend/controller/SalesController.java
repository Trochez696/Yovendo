package com.yovendo.backend.controller;

import com.yovendo.backend.dto.SaleDTO;
import com.yovendo.backend.entity.User;
import com.yovendo.backend.repository.UserRepository;
import com.yovendo.backend.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SalesController {

    private final SaleService saleService;
    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'CONSULTOR')")
    public ResponseEntity<List<SaleDTO>> listSales(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        return ResponseEntity.ok(saleService.getSalesForUser(user));
    }

    @PostMapping
    @PreAuthorize("hasRole('CONSULTOR')")
    public ResponseEntity<SaleDTO> createSale(@RequestBody SaleDTO request, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(saleService.createSale(request, userDetails.getUsername()));
    }
}
