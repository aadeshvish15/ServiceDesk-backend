package com.servicedesk.servicedesk_backend.notification.dto;

// STEP 1: IMPORT REQUIRED CLASSES

import com.servicedesk.servicedesk_backend.notification.NotificationType;

import java.time.LocalDateTime;


// STEP 2: DEFINE NOTIFICATION RESPONSE

public record NotificationResponse(

        Long id,

        Long ticketId,

        String ticketSubject,

        NotificationType type,

        String title,

        String message,

        boolean read,

        LocalDateTime createdAt,

        LocalDateTime readAt

) {
}