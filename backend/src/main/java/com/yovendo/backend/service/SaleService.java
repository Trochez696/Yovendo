package com.yovendo.backend.service;

import com.yovendo.backend.dto.SaleDTO;
import com.yovendo.backend.entity.Sale;
import com.yovendo.backend.entity.User;
import com.yovendo.backend.repository.SaleRepository;
import com.yovendo.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public List<SaleDTO> getSalesForUser(User user) {
        // El consultor solo consulta sus ventas; administracion y direccion consultan todo.
        boolean isConsultor = user.getRoles().stream().anyMatch(r -> "CONSULTOR".equalsIgnoreCase(r.getName()));
        List<Sale> sales = isConsultor ? saleRepository.findByConsultantIdOrderBySaleDateDesc(user.getId()) : saleRepository.findAll();
        return sales.stream().map(this::toDTO).toList();
    }

    @Transactional
    public SaleDTO createSale(SaleDTO dto, String username) {
        // La venta se asigna al usuario autenticado y dispara notificacion a direccion.
        User consultant = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario consultor no encontrado"));

        Sale sale = Sale.builder()
                .clientName(dto.getClientName())
                .clientEmail(dto.getClientEmail())
                .clientPhone(dto.getClientPhone())
                .amount(dto.getAmount())
                .description(dto.getDescription())
                .consultant(consultant)
                .build();

        sale = saleRepository.save(sale);
        notificationService.notifyNewSale(consultant.getUsername(), sale.getAmount());
        return toDTO(sale);
    }

    private SaleDTO toDTO(Sale sale) {
        return SaleDTO.builder()
                .id(sale.getId())
                .clientName(sale.getClientName())
                .clientEmail(sale.getClientEmail())
                .clientPhone(sale.getClientPhone())
                .amount(sale.getAmount())
                .description(sale.getDescription())
                .saleDate(sale.getSaleDate())
                .consultantId(sale.getConsultant() != null ? sale.getConsultant().getId() : null)
                .consultantName(sale.getConsultant() != null ? sale.getConsultant().getUsername() : null)
                .build();
    }
}
