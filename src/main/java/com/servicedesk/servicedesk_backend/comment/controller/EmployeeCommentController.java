package com.servicedesk.servicedesk_backend.comment.controller;

// STEP 1: IMPORT REQUIRED CLASSES

import com.servicedesk.servicedesk_backend.comment.CommentService;
import com.servicedesk.servicedesk_backend.comment.dto.CommentResponse;
import com.servicedesk.servicedesk_backend.comment.dto.PublicCommentRequest;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.web.bind.annotation.*;

import java.util.List;


// STEP 2: DECLARE EMPLOYEE COMMENT CONTROLLER

@RestController
@RequestMapping("/api/employee/tickets/{ticketId}/comments")
public class EmployeeCommentController {

    // STEP 3: INJECT COMMENT SERVICE
    private final CommentService commentService;


    // STEP 4: CONSTRUCTOR INJECTION
    public EmployeeCommentController(CommentService commentService) {

        this.commentService = commentService;
    }


    // STEP 5: EMPLOYEE ADDS A PUBLIC COMMENT

    @PostMapping
    public ResponseEntity<CommentResponse> addComment(

            @PathVariable Long ticketId,

            @Valid @RequestBody PublicCommentRequest request,

            @AuthenticationPrincipal UserDetails userDetails
    ) {

        // STEP 5.1: GET LOGGED-IN EMPLOYEE EMAIL
        String employeeEmail = userDetails.getUsername();


        // STEP 5.2: CALL COMMENT SERVICE
        CommentResponse response = commentService.addEmployeeComment(
                ticketId,
                employeeEmail,
                request
        );


        // STEP 5.3: RETURN HTTP 201 CREATED
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    // STEP 6: EMPLOYEE FETCHES PUBLIC COMMENTS

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(

            @PathVariable Long ticketId,

            @AuthenticationPrincipal UserDetails userDetails
    ) {

        // STEP 6.1: GET LOGGED-IN EMPLOYEE EMAIL
        String employeeEmail = userDetails.getUsername();


        // STEP 6.2: FETCH EMPLOYEE COMMENTS
        List<CommentResponse> comments = commentService.getEmployeeComments(
                ticketId,
                employeeEmail
        );


        // STEP 6.3: RETURN COMMENT LIST
        return ResponseEntity.ok(comments);
    }

}