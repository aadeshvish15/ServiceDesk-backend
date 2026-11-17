package com.servicedesk.servicedesk_backend.comment.dto;

// STEP 1: IMPORT REQUIRED CLASSES

import com.servicedesk.servicedesk_backend.comment.CommentVisibility;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


// STEP 2: DEFINE AGENT COMMENT REQUEST

public record AgentCommentRequest(

        // STEP 3: VALIDATE COMMENT CONTENT
        @NotBlank(message = "Comment content is required")
        @Size(max = 5000, message = "Comment cannot exceed 5000 characters")
        String content,

        // STEP 4: VALIDATE COMMENT VISIBILITY
        @NotNull(message = "Comment visibility is required")
        CommentVisibility visibility

) {
}