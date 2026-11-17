package com.servicedesk.servicedesk_backend.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    Page<Notification> findByRecipient_IdOrderByCreatedAtDesc(
            Long recipientId,
            Pageable pageable
    );

    long countByRecipient_IdAndReadAtIsNull(Long recipientId);

    Optional<Notification> findByIdAndRecipient_Id(
            Long notificationId,
            Long recipientId
    );

    List<Notification> findByRecipient_IdAndReadAtIsNullOrderByCreatedAtDesc(
            Long recipientId
    );
}