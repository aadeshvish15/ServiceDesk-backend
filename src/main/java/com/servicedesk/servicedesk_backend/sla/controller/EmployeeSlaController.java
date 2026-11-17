package com.servicedesk.servicedesk_backend.sla.controller;

// STEP 1: IMPORT REQUIRED CLASSES

import com.servicedesk.servicedesk_backend.sla.SlaTrackingService;
import com.servicedesk.servicedesk_backend.sla.dto.SlaStatusResponse;

import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.web.bind.annotation.*;


// STEP 2: DECLARE EMPLOYEE SLA CONTROLLER

@RestController
@RequestMapping("/api/employee/tickets/{ticketId}/sla")
public class EmployeeSlaController {

    // STEP 3: INJECT SLA TRACKING SERVICE
    private final SlaTrackingService slaTrackingService;


    // STEP 4: CONSTRUCTOR INJECTION
    public EmployeeSlaController(SlaTrackingService slaTrackingService) {

        this.slaTrackingService = slaTrackingService;
    }


    // STEP 5: FETCH SLA DETAILS FOR EMPLOYEE TICKET

    @GetMapping
    public ResponseEntity<SlaStatusResponse> getSlaStatus(

            @PathVariable Long ticketId,

            @AuthenticationPrincipal UserDetails userDetails
    ) {

        // STEP 5.1: GET LOGGED-IN EMPLOYEE EMAIL
        String employeeEmail = userDetails.getUsername();


        // STEP 5.2: FETCH SLA STATUS
        SlaStatusResponse response =
                slaTrackingService.getEmployeeSla(
                        ticketId,
                        employeeEmail
                );


        // STEP 5.3: RETURN SLA STATUS
        return ResponseEntity.ok(response);
    }

}