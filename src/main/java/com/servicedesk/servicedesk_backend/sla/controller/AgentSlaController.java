package com.servicedesk.servicedesk_backend.sla.controller;

// STEP 1: IMPORT REQUIRED CLASSES

import com.servicedesk.servicedesk_backend.sla.SlaTrackingService;
import com.servicedesk.servicedesk_backend.sla.dto.SlaStatusResponse;

import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.web.bind.annotation.*;


// STEP 2: DECLARE AGENT SLA CONTROLLER

@RestController
@RequestMapping("/api/agent/tickets/{ticketId}/sla")
public class AgentSlaController {

    // STEP 3: INJECT SLA TRACKING SERVICE
    private final SlaTrackingService slaTrackingService;


    // STEP 4: CONSTRUCTOR INJECTION
    public AgentSlaController(SlaTrackingService slaTrackingService) {

        this.slaTrackingService = slaTrackingService;
    }


    // STEP 5: FETCH SLA DETAILS FOR AGENT OR ADMIN

    @GetMapping
    public ResponseEntity<SlaStatusResponse> getSlaStatus(

            @PathVariable Long ticketId,

            @AuthenticationPrincipal UserDetails userDetails
    ) {

        // STEP 5.1: GET LOGGED-IN USER EMAIL
        String currentUserEmail = userDetails.getUsername();


        // STEP 5.2: CHECK ADMIN ROLE
        boolean isAdmin = userDetails.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN")
                );


        // STEP 5.3: FETCH SLA DETAILS
        SlaStatusResponse response =
                slaTrackingService.getAgentSla(
                        ticketId,
                        currentUserEmail,
                        isAdmin
                );


        // STEP 5.4: RETURN SLA STATUS
        return ResponseEntity.ok(response);
    }

}