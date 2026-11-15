
package com.servicedesk.servicedesk_backend.category.dto;

import java.time.LocalDateTime;

// STEP 1: DEFINE CATEGORY RESPONSE DTO

// Controls which category information is returned to the frontend.
public record CategoryResponse(

        Long id,
        String name,
        String description,
        boolean active,
        LocalDateTime createdAt

) {
}
