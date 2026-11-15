
package com.servicedesk.servicedesk_backend.category.dto;

import jakarta.validation.constraints.NotNull;

// STEP 1: DEFINE CATEGORY STATUS REQUEST DTO

// Used when an admin activates or deactivates a category.
public record CategoryStatusRequest(

        @NotNull(message = "Active status is required")
        Boolean active

) {
}
