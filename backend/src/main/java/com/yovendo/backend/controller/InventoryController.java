package com.yovendo.backend.controller;

import com.yovendo.backend.dto.InventoryItemDTO;
import com.yovendo.backend.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    // Servicio encargado de crear, actualizar, buscar y ajustar insumos.
    private final InventoryService inventoryService;

    // Lista todo el inventario para perfiles autorizados.
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR', 'DIRECTOR')")
    public ResponseEntity<List<InventoryItemDTO>> listInventory() {
        return ResponseEntity.ok(inventoryService.getAllItems());
    }

    // Consulta un insumo especifico por su id.
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR', 'DIRECTOR')")
    public ResponseEntity<InventoryItemDTO> getItem(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getItemById(id));
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR', 'DIRECTOR')")
    public ResponseEntity<List<InventoryItemDTO>> getLowStockItems() {
        // Lista insumos que estan en o por debajo del minimo configurado.
        return ResponseEntity.ok(inventoryService.getLowStockItems());
    }

    // Busca insumos por nombre usando el parametro q de la URL.
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR', 'DIRECTOR')")
    public ResponseEntity<List<InventoryItemDTO>> searchInventory(@RequestParam String q) {
        return ResponseEntity.ok(inventoryService.searchItems(q));
    }

    // Crea un insumo nuevo. Solo el supervisor modifica inventario.
    @PostMapping
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<InventoryItemDTO> createInventory(@RequestBody InventoryItemDTO dto) {
        return ResponseEntity.ok(inventoryService.createItem(dto));
    }

    // Actualiza parcialmente un insumo existente.
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<InventoryItemDTO> updateInventory(@PathVariable Long id, @RequestBody InventoryItemDTO dto) {
        return ResponseEntity.ok(inventoryService.updateItem(id, dto));
    }

    // Elimina un insumo por id.
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<Void> deleteInventory(@PathVariable Long id) {
        inventoryService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/adjust")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<InventoryItemDTO> adjustQuantity(@PathVariable Long id, @RequestBody Map<String, Integer> request) {
        // Ajuste incremental: puede sumar entradas o restar salidas de inventario.
        int adjustment = request.get("adjustment");
        return ResponseEntity.ok(inventoryService.adjustQuantity(id, adjustment));
    }
}
