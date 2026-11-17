package com.servicedesk.servicedesk_backend.activity.controller;

// STEP 1: IMPORT REQUIRED CLASSES

import com.servicedesk.servicedesk_backend.activity.ActivityService;
import com.servicedesk.servicedesk_backend.activity.dto.ActivityResponse;

import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.web.bind.annotation.*;

import java.util.List;


// STEP 2: DECLARE AGENT ACTIVITY CONTROLLER

@RestController
@RequestMapping("/api/agent/tickets/{ticketId}/activities")
public class AgentActivityController {

    // STEP 3: INJECT ACTIVITY SERVICE
    private final ActivityService activityService;


    // STEP 4: CONSTRUCTOR INJECTION
    public AgentActivityController(ActivityService activityService) {

        this.activityService = activityService;
    }


    // STEP 5: FETCH AGENT OR ADMIN ACTIVITY HISTORY

    @GetMapping
    public ResponseEntity<List<ActivityResponse>> getActivities(

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


        // STEP 5.3: FETCH ACTIVITIES
        List<ActivityResponse> activities =
                activityService.getAgentActivities(
                        ticketId,
                        currentUserEmail,
                        isAdmin
                );


        // STEP 5.4: RETURN ACTIVITY HISTORY
        return ResponseEntity.ok(activities);
    }

}