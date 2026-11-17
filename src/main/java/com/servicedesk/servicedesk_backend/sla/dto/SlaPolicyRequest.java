package com.servicedesk.servicedesk_backend.sla.dto;

// STEP 1: IMPORT REQUIRED CLASSES

import com.servicedesk.servicedesk_backend.ticket.TicketPriority;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


// STEP 2: DEFINE SLA POLICY REQUEST

public record SlaPolicyRequest(

        // STEP 3: VALIDATE PRIORITY
        @NotNull(message = "Ticket priority is required")
        TicketPriority priority,

        // STEP 4: VALIDATE FIRST RESPONSE MINUTES
        @NotNull(message = "First response time is required")
        @Positive(message = "First response time must be positive")
        Integer firstResponseMinutes,

        // STEP 5: VALIDATE RESOLUTION MINUTES
        @NotNull(message = "Resolution time is required")
        @Positive(message = "Resolution time must be positive")
        Integer resolutionMinutes

) {
}