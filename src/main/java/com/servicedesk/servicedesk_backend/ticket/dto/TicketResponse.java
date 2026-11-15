package com.servicedesk.servicedesk_backend.ticket.dto;

// STEP 1: IMPORT REQUIRED CLASSES

import com.servicedesk.servicedesk_backend.ticket.TicketPriority;
import com.servicedesk.servicedesk_backend.ticket.TicketStatus;

import java.time.LocalDateTime;


// STEP 2: DEFINE TICKET RESPONSE

// This DTO prevents our JPA entities from being returned directly.
public record TicketResponse(

        Long id,

        String subject,

        String description,

        TicketPriority priority,

        TicketStatus status,

        Long categoryId,

        String categoryName,

        Long createdById,

        String createdByName,

        String createdByEmail,

        Long assignedToId,

        String assignedToName,

        String assignedToEmail,

        LocalDateTime createdAt,

        LocalDateTime updatedAt,

        LocalDateTime resolvedAt

) {
}