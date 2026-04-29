package com.yovendo.backend.service;

import com.yovendo.backend.dto.NotificationDTO;
import com.yovendo.backend.entity.Notification;
import com.yovendo.backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public List<NotificationDTO> getNotificationsForUser(String role, Long userId) {
        List<Notification> notifications;
        
        // Primero notificaciones específicas del usuario
        List<Notification> userNotifications = notificationRepository.findByRecipientIdAndReadFalse(userId);
        
        // Luego notificaciones del rol
        List<Notification> roleNotifications = notificationRepository.findByRecipientRoleAndReadFalse(role);
        
        // Combinar y eliminar duplicados
        notifications = new java.util.ArrayList<>(userNotifications);
        for (Notification n : roleNotifications) {
            if (!notifications.stream().anyMatch(n2 -> n2.getId().equals(n.getId()))) {
                notifications.add(n);
            }
        }
        
        return notifications.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::toDTO)
                .toList();
    }

    public List<NotificationDTO> getAllNotifications(String role, Long userId) {
        return notificationRepository.findByRecipientRoleOrRecipientIdIsNullOrderByCreatedAtDesc(role)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public NotificationDTO createNotification(String message, String recipientRole, Long recipientId, String type) {
        Notification notification = Notification.builder()
                .message(message)
                .recipientRole(recipientRole)
                .recipientId(recipientId)
                .type(type)
                .read(false)
                .build();
        
        notification = notificationRepository.save(notification);
        return toDTO(notification);
    }

    @Transactional
    public void markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(String role, Long userId) {
        List<Notification> notifications = notificationRepository.findByRecipientRoleAndReadFalse(role);
        notifications.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(notifications);
        
        List<Notification> userNotifications = notificationRepository.findByRecipientIdAndReadFalse(userId);
        userNotifications.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(userNotifications);
    }

    @Transactional
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }

    // Notificaciones automáticas para inventario bajo
    public void notifyLowStock(String itemName, int currentQuantity) {
        createNotification(
                "Stock bajo: " + itemName + " (cantidad: " + currentQuantity + ")",
                "SUPERVISOR",
                null,
                "LOW_STOCK"
        );
    }

    // Notificación de nueva venta
    public void notifyNewSale(String consultantName, double total) {
        createNotification(
                "Nueva venta realizada por " + consultantName + " - Total: $" + total,
                "DIRECTOR",
                null,
                "NEW_SALE"
        );
    }

    private NotificationDTO toDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .recipientRole(notification.getRecipientRole())
                .recipientId(notification.getRecipientId())
                .read(notification.isRead())
                .type(notification.getType())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}