package com.servicedesk.servicedesk_backend.comment.dto;

// STEP 1: IMPORT VALIDATION ANNOTATIONS

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


// STEP 2: DEFINE PUBLIC COMMENT REQUEST

public record PublicCommentRequest(

        // STEP 3: VALIDATE COMMENT CONTENT
        @NotBlank(message = "Comment content is required")
        @Size(max = 5000, message = "Comment cannot exceed 5000 characters")
        String content

) {
}