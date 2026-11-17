package com.servicedesk.servicedesk_backend.sla.dto;

// STEP 1: IMPORT REQUIRED CLASSES

import com.servicedesk.servicedesk_backend.ticket.TicketPriority;

import java.time.LocalDateTime;


// STEP 2: DEFINE SLA POLICY RESPONSE

public record SlaPolicyResponse(

        Long id,

        TicketPriority priority,

        Integer firstResponseMinutes,

        Integer resolutionMinutes,

        boolean active,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}