package com.servicedesk.servicedesk_backend.auth.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// STEP 1: DEFINE LOGIN REQUEST DTO
// This class receives credentials from the login form.

public record LoginRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email address")
        String email,

        @NotBlank(message = "Password is required")
        String password

) {
}
