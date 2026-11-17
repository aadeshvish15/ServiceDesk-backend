package com.servicedesk.servicedesk_backend.activity.controller;

// STEP 1: IMPORT REQUIRED CLASSES

import com.servicedesk.servicedesk_backend.activity.ActivityService;
import com.servicedesk.servicedesk_backend.activity.dto.ActivityResponse;

import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.web.bind.annotation.*;

import java.util.List;


// STEP 2: DECLARE EMPLOYEE ACTIVITY CONTROLLER

@RestController
@RequestMapping("/api/employee/tickets/{ticketId}/activities")
public class EmployeeActivityController {

    // STEP 3: INJECT ACTIVITY SERVICE
    private final ActivityService activityService;


    // STEP 4: CONSTRUCTOR INJECTION
    public EmployeeActivityController(ActivityService activityService) {

        this.activityService = activityService;
    }


    // STEP 5: FETCH EMPLOYEE'S PUBLIC ACTIVITY HISTORY

    @GetMapping
    public ResponseEntity<List<ActivityResponse>> getActivities(

            @PathVariable Long ticketId,

            @AuthenticationPrincipal UserDetails userDetails
    ) {

        // STEP 5.1: GET LOGGED-IN EMPLOYEE EMAIL
        String employeeEmail = userDetails.getUsername();


        // STEP 5.2: FETCH PUBLIC ACTIVITIES
        List<ActivityResponse> activities =
                activityService.getEmployeeActivities(
                        ticketId,
                        employeeEmail
                );


        // STEP 5.3: RETURN ACTIVITY HISTORY
        return ResponseEntity.ok(activities);
    }

}