
package com.servicedesk.servicedesk_backend.auth.dto;

// STEP 1: DEFINE AUTHENTICATION RESPONSE DTO
// This class returns the JWT token and authenticated user information.

public record AuthResponse(

        String accessToken,
        String tokenType,
        UserResponse user

) {
}
