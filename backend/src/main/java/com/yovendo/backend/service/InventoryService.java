package com.yovendo.backend.service;

import com.yovendo.backend.dto.InventoryItemDTO;
import com.yovendo.backend.entity.InventoryItem;
import com.yovendo.backend.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    // Acceso a la tabla de insumos.
    private final InventoryRepository inventoryRepository;
    // Se usa para generar alertas cuando el stock queda bajo.
    private final NotificationService notificationService;

    // Devuelve todos los insumos convertidos a DTO.
    public List<InventoryItemDTO> getAllItems() {
        return inventoryRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    // Busca un insumo por id; si no existe, corta el flujo con una excepcion.
    public InventoryItemDTO getItemById(Long id) {
        InventoryItem item = inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado"));
        return toDTO(item);
    }

    // Filtra en memoria los insumos que estan en minimo o por debajo del minimo.
    public List<InventoryItemDTO> getLowStockItems() {
        return inventoryRepository.findAll().stream()
                .filter(item -> item.getQuantity() <= item.getMinStock())
                .map(this::toDTO)
                .toList();
    }

    // Busca por nombre ignorando mayusculas/minusculas.
    public List<InventoryItemDTO> searchItems(String query) {
        return inventoryRepository.findByNameContainingIgnoreCase(query).stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public InventoryItemDTO createItem(InventoryItemDTO dto) {
        // Guarda el insumo y notifica si nace con stock bajo.
        InventoryItem item = InventoryItem.builder()
                .name(dto.getName())
                .quantity(dto.getQuantity())
                .description(dto.getDescription())
                .unit(dto.getUnit())
                .price(dto.getPrice())
                .minStock(dto.getMinStock())
                .maxStock(dto.getMaxStock())
                .build();
        
        item = inventoryRepository.save(item);
        notifyLowStockIfNeeded(item);
        return toDTO(item);
    }

    @Transactional
    public InventoryItemDTO updateItem(Long id, InventoryItemDTO dto) {
        // Actualizacion parcial: solo se cambian campos enviados con valores validos.
        InventoryItem item = inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado"));

        if (dto.getName() != null) item.setName(dto.getName());
        if (dto.getQuantity() > 0) item.setQuantity(dto.getQuantity());
        if (dto.getDescription() != null) item.setDescription(dto.getDescription());
        if (dto.getUnit() != null) item.setUnit(dto.getUnit());
        if (dto.getPrice() > 0) item.setPrice(dto.getPrice());
        if (dto.getMinStock() > 0) item.setMinStock(dto.getMinStock());
        if (dto.getMaxStock() > 0) item.setMaxStock(dto.getMaxStock());

        item = inventoryRepository.save(item);
        notifyLowStockIfNeeded(item);
        return toDTO(item);
    }

    @Transactional
    public void deleteItem(Long id) {
        if (!inventoryRepository.existsById(id)) {
            throw new RuntimeException("Insumo no encontrado");
        }
        inventoryRepository.deleteById(id);
    }

    @Transactional
    public InventoryItemDTO adjustQuantity(Long id, int adjustment) {
        // Protege el inventario para que ningun ajuste deje cantidades negativas.
        InventoryItem item = inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado"));
        
        int newQuantity = item.getQuantity() + adjustment;
        if (newQuantity < 0) {
            throw new RuntimeException("La cantidad no puede ser negativa");
        }
        
        item.setQuantity(newQuantity);
        item = inventoryRepository.save(item);
        notifyLowStockIfNeeded(item);
        return toDTO(item);
    }

    private void notifyLowStockIfNeeded(InventoryItem item) {
        if (item.getQuantity() > item.getMinStock()) {
            return;
        }

        try {
            notificationService.notifyLowStock(item.getName(), item.getQuantity());
        } catch (RuntimeException ex) {
            log.warn("No se pudo crear la notificacion de stock bajo para el insumo {}", item.getId(), ex);
        }
    }

    private InventoryItemDTO toDTO(InventoryItem item) {
        // Convierte la entidad JPA a un objeto simple para enviar por JSON.
        return InventoryItemDTO.builder()
                .id(item.getId())
                .name(item.getName())
                .quantity(item.getQuantity())
                .description(item.getDescription())
                .unit(item.getUnit())
                .price(item.getPrice())
                .minStock(item.getMinStock())
                .maxStock(item.getMaxStock())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
