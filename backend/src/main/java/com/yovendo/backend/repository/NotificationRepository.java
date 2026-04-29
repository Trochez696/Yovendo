package com.yovendo.backend.repository;

import com.yovendo.backend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientRoleAndReadFalse(String recipientRole);
    List<Notification> findByRecipientIdAndReadFalse(Long recipientId);
    List<Notification> findByRecipientRoleOrRecipientIdIsNullOrderByCreatedAtDesc(String recipientRole);
}