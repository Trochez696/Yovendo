package com.yovendo.backend.controller;

import com.yovendo.backend.dto.NotificationDTO;
import com.yovendo.backend.dto.UserDTO;
import com.yovendo.backend.service.AuthService;
import com.yovendo.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> listNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        // Filtra las notificaciones segun rol o usuario especifico.
        UserDTO user = authService.getCurrentUser(userDetails.getUsername());
        String role = user.getRoles().isEmpty() ? null : user.getRoles().get(0);
        return ResponseEntity.ok(notificationService.getNotificationsForUser(role, user.getId()));
    }

    @GetMapping("/all")
    public ResponseEntity<List<NotificationDTO>> getAllNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        UserDTO user = authService.getCurrentUser(userDetails.getUsername());
        String role = user.getRoles().isEmpty() ? null : user.getRoles().get(0);
        return ResponseEntity.ok(notificationService.getAllNotifications(role, user.getId()));
    }

    @PostMapping
    public ResponseEntity<NotificationDTO> createNotification(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(notificationService.createNotification(
                request.get("message"),
                request.get("recipientRole"),
                null,
                request.get("type")
        ));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal UserDetails userDetails) {
        // Marca como leidas todas las notificaciones visibles para el usuario actual.
        UserDTO user = authService.getCurrentUser(userDetails.getUsername());
        String role = user.getRoles().isEmpty() ? null : user.getRoles().get(0);
        notificationService.markAllAsRead(role, user.getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
}
