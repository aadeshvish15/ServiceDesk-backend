package com.servicedesk.servicedesk_backend.sla.dto;

// STEP 1: IMPORT VALIDATION ANNOTATION

import jakarta.validation.constraints.NotNull;


// STEP 2: DEFINE SLA STATUS REQUEST

public record SlaPolicyStatusRequest(

        @NotNull(message = "Active status is required")
        Boolean active

) {
}