package com.yovendo.backend.controller;

import com.yovendo.backend.repository.CallRepository;
import com.yovendo.backend.repository.InventoryRepository;
import com.yovendo.backend.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivitiesController {

    // Repositorios usados para contar registros y construir el resumen general.
    private final SaleRepository saleRepository;
    private final CallRepository callRepository;
    private final InventoryRepository inventoryRepository;

    // GET /api/activities/summary
    // Solo ADMIN y DIRECTOR pueden ver indicadores globales de ventas, llamadas e inventario.
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<Map<String, Object>> summary() {
        // Resumen ejecutivo usado por perfiles que necesitan indicadores globales.
        long totalSales = saleRepository.count();
        long totalCalls = callRepository.count();
        long totalInventoryItems = inventoryRepository.count();
        long lowStock = inventoryRepository.findAll().stream().filter(i -> i.getQuantity() <= i.getMinStock()).count();

        return ResponseEntity.ok(Map.of(
                "totalSales", totalSales,
                "totalCalls", totalCalls,
                "totalInventoryItems", totalInventoryItems,
                "lowStockItems", lowStock
        ));
    }
}
