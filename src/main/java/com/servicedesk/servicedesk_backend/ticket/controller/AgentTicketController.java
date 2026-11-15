package com.servicedesk.servicedesk_backend.ticket.controller;

// STEP 1: IMPORT REQUIRED CLASSES

import com.servicedesk.servicedesk_backend.ticket.TicketService;

import com.servicedesk.servicedesk_backend.ticket.dto.TicketResponse;
import com.servicedesk.servicedesk_backend.ticket.dto.TicketStatusUpdateRequest;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.web.bind.annotation.*;

import java.util.List;


// STEP 2: DECLARE AGENT TICKET CONTROLLER

@RestController
@RequestMapping("/api/agent/tickets")
public class AgentTicketController {

    // STEP 3: INJECT TICKET SERVICE
    private final TicketService ticketService;


    // STEP 4: CONSTRUCTOR INJECTION
    public AgentTicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }


    // STEP 5: FETCH ALL TICKETS

    @GetMapping
    public ResponseEntity<List<TicketResponse>> getAllTickets() {

        // STEP 5.1: FETCH ALL TICKETS
        List<TicketResponse> tickets = ticketService.getAllTickets();


        // STEP 5.2: RETURN TICKET LIST
        return ResponseEntity.ok(tickets);
    }


    // STEP 6: FETCH UNASSIGNED TICKETS

    @GetMapping("/unassigned")
    public ResponseEntity<List<TicketResponse>> getUnassignedTickets() {

        // STEP 6.1: FETCH UNASSIGNED TICKETS
        List<TicketResponse> tickets =
                ticketService.getUnassignedTickets();


        // STEP 6.2: RETURN RESULT
        return ResponseEntity.ok(tickets);
    }


    // STEP 7: FETCH TICKET DETAILS

    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketResponse> getTicket(

            @PathVariable Long ticketId
    ) {

        // STEP 7.1: FETCH TICKET
        TicketResponse response =
                ticketService.getTicket(ticketId);


        // STEP 7.2: RETURN TICKET DETAILS
        return ResponseEntity.ok(response);
    }


    // STEP 8: UPDATE TICKET STATUS

    @PatchMapping("/{ticketId}/status")
    public ResponseEntity<TicketResponse> updateTicketStatus(

            @PathVariable Long ticketId,

            @Valid @RequestBody TicketStatusUpdateRequest request,

            @AuthenticationPrincipal UserDetails userDetails
    ) {

        // STEP 8.1: GET LOGGED-IN USER EMAIL
        String currentUserEmail = userDetails.getUsername();


        // STEP 8.2: CHECK WHETHER LOGGED-IN USER IS ADMIN

        // ADMIN can update any ticket.
        // AGENT can only update tickets assigned to them.
        boolean isAdmin = userDetails.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN")
                );


        // STEP 8.3: CALL SERVICE TO UPDATE STATUS
        TicketResponse response = ticketService.updateTicketStatus(
                ticketId,
                currentUserEmail,
                request,
                isAdmin
        );


        // STEP 8.4: RETURN UPDATED TICKET
        return ResponseEntity.ok(response);
    }

}