package com.servicedesk.servicedesk_backend.notification.controller;

// STEP 1: IMPORT REQUIRED CLASSES

import com.servicedesk.servicedesk_backend.notification.NotificationService;

import com.servicedesk.servicedesk_backend.notification.dto.NotificationPageResponse;
import com.servicedesk.servicedesk_backend.notification.dto.NotificationResponse;
import com.servicedesk.servicedesk_backend.notification.dto.NotificationUnreadCountResponse;
import com.servicedesk.servicedesk_backend.notification.dto.NotificationMarkAllReadResponse;

import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.web.bind.annotation.*;


// STEP 2: DECLARE NOTIFICATION CONTROLLER

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    // STEP 3: INJECT NOTIFICATION SERVICE

    private final NotificationService notificationService;


    // STEP 4: CONSTRUCTOR INJECTION

    public NotificationController(
            NotificationService notificationService
    ) {

        this.notificationService = notificationService;
    }


    // STEP 5: FETCH LOGGED-IN USER'S NOTIFICATIONS

    @GetMapping
    public ResponseEntity<NotificationPageResponse> getMyNotifications(

            @AuthenticationPrincipal UserDetails userDetails,

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "20") int size
    ) {

        // STEP 5.1: GET LOGGED-IN USER EMAIL

        String email = userDetails.getUsername();


        // STEP 5.2: FETCH NOTIFICATIONS

        NotificationPageResponse response =
                notificationService.getMyNotifications(
                        email,
                        page,
                        size
                );


        // STEP 5.3: RETURN NOTIFICATIONS

        return ResponseEntity.ok(response);
    }


    // STEP 6: FETCH UNREAD NOTIFICATION COUNT

    @GetMapping("/unread-count")
    public ResponseEntity<NotificationUnreadCountResponse> getUnreadCount(

            @AuthenticationPrincipal UserDetails userDetails
    ) {

        // STEP 6.1: GET LOGGED-IN USER EMAIL

        String email = userDetails.getUsername();


        // STEP 6.2: FETCH UNREAD COUNT

        long unreadCount =
                notificationService.getUnreadCount(email);


        // STEP 6.3: CREATE RESPONSE

        NotificationUnreadCountResponse response =
                new NotificationUnreadCountResponse(unreadCount);


        // STEP 6.4: RETURN RESPONSE

        return ResponseEntity.ok(response);
    }


    // STEP 7: MARK ONE NOTIFICATION AS READ

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markAsRead(

            @PathVariable Long notificationId,

            @AuthenticationPrincipal UserDetails userDetails
    ) {

        // STEP 7.1: GET LOGGED-IN USER EMAIL

        String email = userDetails.getUsername();


        // STEP 7.2: MARK NOTIFICATION AS READ

        NotificationResponse response =
                notificationService.markAsRead(
                        notificationId,
                        email
                );


        // STEP 7.3: RETURN UPDATED NOTIFICATION

        return ResponseEntity.ok(response);
    }


    // STEP 8: MARK ALL NOTIFICATIONS AS READ

    @PatchMapping("/read-all")
    public ResponseEntity<NotificationMarkAllReadResponse> markAllAsRead(

            @AuthenticationPrincipal UserDetails userDetails
    ) {

        // STEP 8.1: GET LOGGED-IN USER EMAIL

        String email = userDetails.getUsername();


        // STEP 8.2: MARK ALL AS READ

        int updatedCount =
                notificationService.markAllAsRead(email);


        // STEP 8.3: CREATE RESPONSE

        NotificationMarkAllReadResponse response =
                new NotificationMarkAllReadResponse(updatedCount);


        // STEP 8.4: RETURN RESPONSE

        return ResponseEntity.ok(response);
    }
}