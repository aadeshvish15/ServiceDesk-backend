package com.servicedesk.servicedesk_backend.auth.dto;
import com.servicedesk.servicedesk_backend.user.Role;

// STEP 1: DEFINE USER RESPONSE DTO
// This class controls which user information is returned to the frontend.

public record UserResponse(

        Long id,
        String name,
        String email,
        Role role

) {
}
