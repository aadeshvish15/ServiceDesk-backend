package com.servicedesk.servicedesk_backend.ticket.dto;

// STEP 1: IMPORT REQUIRED CLASSES

import com.servicedesk.servicedesk_backend.ticket.TicketStatus;

import jakarta.validation.constraints.NotNull;


// STEP 2: DEFINE STATUS UPDATE REQUEST

public record TicketStatusUpdateRequest(

        // STEP 3: VALIDATE NEW STATUS
        @NotNull(message = "Ticket status is required")
        TicketStatus status

) {
}