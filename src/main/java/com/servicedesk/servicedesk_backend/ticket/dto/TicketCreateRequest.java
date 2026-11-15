package com.servicedesk.servicedesk_backend.ticket.dto;

// STEP 1: IMPORT VALIDATION ANNOTATIONS

import com.servicedesk.servicedesk_backend.ticket.TicketPriority;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;


// STEP 2: DEFINE TICKET CREATION REQUEST

public record TicketCreateRequest(

        // STEP 3: VALIDATE TICKET SUBJECT
        @NotBlank(message = "Ticket subject is required")
        @Size(max = 150, message = "Subject cannot exceed 150 characters")
        String subject,

        // STEP 4: VALIDATE DESCRIPTION
        @NotBlank(message = "Ticket description is required")
        @Size(max = 5000, message = "Description cannot exceed 5000 characters")
        String description,

        // STEP 5: VALIDATE PRIORITY
        @NotNull(message = "Ticket priority is required")
        TicketPriority priority,

        // STEP 6: VALIDATE CATEGORY ID
        @NotNull(message = "Category ID is required")
        @Positive(message = "Category ID must be positive")
        Long categoryId

) {
}