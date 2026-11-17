package com.servicedesk.servicedesk_backend.comment.controller;

// STEP 1: IMPORT REQUIRED CLASSES

import com.servicedesk.servicedesk_backend.comment.CommentService;
import com.servicedesk.servicedesk_backend.comment.dto.AgentCommentRequest;
import com.servicedesk.servicedesk_backend.comment.dto.CommentResponse;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.web.bind.annotation.*;

import java.util.List;


// STEP 2: DECLARE AGENT COMMENT CONTROLLER

@RestController
@RequestMapping("/api/agent/tickets/{ticketId}/comments")
public class AgentCommentController {

    // STEP 3: INJECT COMMENT SERVICE
    private final CommentService commentService;


    // STEP 4: CONSTRUCTOR INJECTION
    public AgentCommentController(CommentService commentService) {

        this.commentService = commentService;
    }


    // STEP 5: AGENT OR ADMIN ADDS COMMENT / INTERNAL NOTE

    @PostMapping
    public ResponseEntity<CommentResponse> addComment(

            @PathVariable Long ticketId,

            @Valid @RequestBody AgentCommentRequest request,

            @AuthenticationPrincipal UserDetails userDetails
    ) {

        // STEP 5.1: GET LOGGED-IN USER EMAIL
        String currentUserEmail = userDetails.getUsername();


        // STEP 5.2: CHECK WHETHER LOGGED-IN USER IS ADMIN
        boolean isAdmin = isAdmin(userDetails);


        // STEP 5.3: CALL COMMENT SERVICE
        CommentResponse response = commentService.addAgentComment(
                ticketId,
                currentUserEmail,
                request,
                isAdmin
        );


        // STEP 5.4: RETURN HTTP 201 CREATED
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    // STEP 6: AGENT OR ADMIN FETCHES ALL COMMENTS

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(

            @PathVariable Long ticketId,

            @AuthenticationPrincipal UserDetails userDetails
    ) {

        // STEP 6.1: GET LOGGED-IN USER EMAIL
        String currentUserEmail = userDetails.getUsername();


        // STEP 6.2: CHECK ADMIN ROLE
        boolean isAdmin = isAdmin(userDetails);


        // STEP 6.3: FETCH COMMENTS
        List<CommentResponse> comments = commentService.getAgentComments(
                ticketId,
                currentUserEmail,
                isAdmin
        );


        // STEP 6.4: RETURN COMMENT LIST
        return ResponseEntity.ok(comments);
    }


    // STEP 7: REUSABLE ADMIN ROLE CHECK

    private boolean isAdmin(UserDetails userDetails) {

        return userDetails.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN")
                );
    }

}