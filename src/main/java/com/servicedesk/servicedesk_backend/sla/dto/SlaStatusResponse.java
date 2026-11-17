package com.servicedesk.servicedesk_backend.sla.dto;

// STEP 1: IMPORT REQUIRED CLASSES

import com.servicedesk.servicedesk_backend.ticket.TicketPriority;
import com.servicedesk.servicedesk_backend.ticket.TicketStatus;

import java.time.LocalDateTime;


// STEP 2: DEFINE TICKET SLA STATUS RESPONSE

public record SlaStatusResponse(

        Long ticketId,

        TicketPriority priority,

        TicketStatus ticketStatus,

        LocalDateTime firstResponseDueAt,

        LocalDateTime firstRespondedAt,

        boolean firstResponseMet,

        boolean firstResponseBreached,

        LocalDateTime resolutionDueAt,

        LocalDateTime resolvedAt,

        boolean resolutionMet,

        boolean resolutionBreached,

        LocalDateTime evaluatedAt

) {
}