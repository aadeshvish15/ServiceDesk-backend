
package com.servicedesk.servicedesk_backend.auth;

// STEP 1: IMPORT REQUIRED CLASSES

import com.servicedesk.servicedesk_backend.auth.dto.AuthResponse;
import com.servicedesk.servicedesk_backend.auth.dto.LoginRequest;
import com.servicedesk.servicedesk_backend.auth.dto.RegisterRequest;
import com.servicedesk.servicedesk_backend.auth.dto.UserResponse;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


// STEP 2: DECLARE REST CONTROLLER

// @RestController handles incoming HTTP requests
// and returns response data in JSON format.
//
// @RequestMapping defines the common URL for all authentication APIs.
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // STEP 3: DECLARE SERVICE DEPENDENCY

    private final AuthService authService;


    // STEP 4: CONSTRUCTOR INJECTION

    // Spring automatically injects AuthService.
    public AuthController(AuthService authService) {

        this.authService = authService;
    }


    // STEP 5: CREATE EMPLOYEE REGISTRATION API

    // HTTP Method: POST
    // Endpoint: /api/auth/register
    //
    // @RequestBody converts incoming JSON into RegisterRequest.
    // @Valid executes the validation annotations in RegisterRequest.
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterRequest request) {


        // STEP 5.1: CALL AUTH SERVICE

        // AuthService handles:
        // 1. Email normalization.
        // 2. Duplicate email checking.
        // 3. Password hashing.
        // 4. Employee account creation.
        // 5. Saving the user in MySQL.
        UserResponse userResponse = authService.register(request);


        // STEP 5.2: RETURN CREATED RESPONSE

        // HTTP 201 indicates that a new employee account was created.
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userResponse);
    }


    // STEP 6: CREATE USER LOGIN API

    // HTTP Method: POST
    // Endpoint: /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {


        // STEP 6.1: CALL AUTH SERVICE

        // AuthService verifies credentials and generates a JWT token.
        AuthResponse authResponse = authService.login(request);


        // STEP 6.2: RETURN LOGIN RESPONSE

        // HTTP 200 indicates successful authentication.
        return ResponseEntity
                .ok(authResponse);
    }


    // STEP 7: CREATE CURRENT USER API

    // HTTP Method: GET
    // Endpoint: /api/auth/me
    //
    // This endpoint returns the currently authenticated user's details.
    // It requires a valid JWT token.
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(

            // STEP 7.1: EXTRACT AUTHENTICATED USER PRINCIPAL

            // Spring Security automatically injects the authenticated
            // UserDetails object from the SecurityContext.
            @AuthenticationPrincipal UserDetails userDetails) {


        // STEP 7.2: EXTRACT USER EMAIL

        // CustomUserDetailsService uses email as the username.
        String email = userDetails.getUsername();


        // STEP 7.3: FETCH CURRENT USER INFORMATION

        // Ask AuthService to fetch the latest user details from MySQL.
        UserResponse userResponse =
                authService.getCurrentUser(email);


        // STEP 7.4: RETURN SUCCESSFUL RESPONSE

        return ResponseEntity.ok(userResponse);
    }
}