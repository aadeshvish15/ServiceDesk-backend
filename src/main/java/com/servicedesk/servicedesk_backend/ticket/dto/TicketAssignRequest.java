package com.servicedesk.servicedesk_backend.ticket.dto;

// STEP 1: IMPORT REQUIRED CLASSES

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


// STEP 2: DEFINE TICKET ASSIGNMENT REQUEST

public record TicketAssignRequest(

        // STEP 3: VALIDATE AGENT ID
        @NotNull(message = "Agent ID is required")
        @Positive(message = "Agent ID must be positive")
        Long agentId

) {
}