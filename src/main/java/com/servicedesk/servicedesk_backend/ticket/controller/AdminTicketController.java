package com.servicedesk.servicedesk_backend.ticket.controller;

// STEP 1: IMPORT REQUIRED CLASSES

import com.servicedesk.servicedesk_backend.ticket.TicketService;

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

    @PatchMapping("/{ticketId}/assignment")
    public ResponseEntity<TicketResponse> assignTicket(

            @PathVariable Long ticketId,

            @Valid @RequestBody TicketAssignRequest request
    ) {

        // STEP 5.1: CALL SERVICE TO ASSIGN TICKET
        TicketResponse response = ticketService.assignTicket(
                ticketId,
                request
        );


        // STEP 5.2: RETURN UPDATED TICKET
        return ResponseEntity.ok(response);
    }

}