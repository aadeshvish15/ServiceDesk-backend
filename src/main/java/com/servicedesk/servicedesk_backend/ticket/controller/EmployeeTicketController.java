package com.servicedesk.servicedesk_backend.ticket.controller;

// STEP 1: IMPORT REQUIRED CLASSES

import com.servicedesk.servicedesk_backend.ticket.TicketService;

import com.servicedesk.servicedesk_backend.ticket.dto.TicketCreateRequest;
import com.servicedesk.servicedesk_backend.ticket.dto.TicketResponse;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.web.bind.annotation.*;

import java.util.List;


// STEP 2: DECLARE EMPLOYEE TICKET CONTROLLER

@RestController
@RequestMapping("/api/employee/tickets")
public class EmployeeTicketController {

    // STEP 3: INJECT TICKET SERVICE
    private final TicketService ticketService;


    // STEP 4: CONSTRUCTOR INJECTION
    public EmployeeTicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }


    // STEP 5: CREATE NEW TICKET

    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(

            @Valid @RequestBody TicketCreateRequest request,

            @AuthenticationPrincipal UserDetails userDetails
    ) {

        // STEP 5.1: GET LOGGED-IN EMPLOYEE EMAIL
        String employeeEmail = userDetails.getUsername();


        // STEP 5.2: CALL SERVICE TO CREATE TICKET
        TicketResponse response = ticketService.createTicket(
                employeeEmail,
                request
        );


        // STEP 5.3: RETURN HTTP 201 CREATED
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    // STEP 6: FETCH ALL TICKETS OF LOGGED-IN EMPLOYEE

    @GetMapping
    public ResponseEntity<List<TicketResponse>> getMyTickets(

            @AuthenticationPrincipal UserDetails userDetails
    ) {

        // STEP 6.1: GET EMPLOYEE EMAIL
        String employeeEmail = userDetails.getUsername();


        // STEP 6.2: FETCH EMPLOYEE TICKETS
        List<TicketResponse> tickets =
                ticketService.getEmployeeTickets(employeeEmail);


        // STEP 6.3: RETURN TICKET LIST
        return ResponseEntity.ok(tickets);
    }


    // STEP 7: FETCH A SINGLE TICKET

    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketResponse> getMyTicket(

            @PathVariable Long ticketId,

            @AuthenticationPrincipal UserDetails userDetails
    ) {

        // STEP 7.1: GET EMPLOYEE EMAIL
        String employeeEmail = userDetails.getUsername();


        // STEP 7.2: FETCH TICKET AND VERIFY OWNERSHIP
        TicketResponse response = ticketService.getEmployeeTicket(
                ticketId,
                employeeEmail
        );


        // STEP 7.3: RETURN TICKET
        return ResponseEntity.ok(response);
    }

}