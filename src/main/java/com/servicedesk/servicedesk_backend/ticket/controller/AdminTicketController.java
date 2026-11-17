package com.servicedesk.servicedesk_backend.ticket.controller;

// STEP 1: IMPORT REQUIRED CLASSES

import com.servicedesk.servicedesk_backend.ticket.TicketService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import com.servicedesk.servicedesk_backend.ticket.dto.TicketAssignRequest;
import com.servicedesk.servicedesk_backend.ticket.dto.TicketResponse;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;


// STEP 2: DECLARE ADMIN TICKET CONTROLLER

@RestController
@RequestMapping("/api/admin/tickets")
public class AdminTicketController {

    // STEP 3: INJECT TICKET SERVICE
    private final TicketService ticketService;


    // STEP 4: CONSTRUCTOR INJECTION
    public AdminTicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }


    // STEP 5: ASSIGN TICKET TO AGENT

// STEP 5: ASSIGN TICKET TO AGENT

    @PatchMapping("/{ticketId}/assignment")
    public ResponseEntity<TicketResponse> assignTicket(

            @PathVariable Long ticketId,

            @Valid @RequestBody TicketAssignRequest request,

            @AuthenticationPrincipal UserDetails userDetails
    ) {

        // STEP 5.1: GET LOGGED-IN ADMIN EMAIL
        String adminEmail = userDetails.getUsername();


        // STEP 5.2: CALL SERVICE TO ASSIGN TICKET
        // Pass ticket ID, admin email and assignment request.
        TicketResponse response = ticketService.assignTicket(
                ticketId,
                adminEmail,
                request
        );


        // STEP 5.3: RETURN UPDATED TICKET
        return ResponseEntity.ok(response);
    }

}