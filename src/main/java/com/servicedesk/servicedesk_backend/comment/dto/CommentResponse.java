package com.servicedesk.servicedesk_backend.comment.dto;

// STEP 1: IMPORT REQUIRED CLASSES

import com.servicedesk.servicedesk_backend.comment.CommentVisibility;
import com.servicedesk.servicedesk_backend.user.Role;

import java.time.LocalDateTime;


// STEP 2: DEFINE COMMENT RESPONSE DTO

public record CommentResponse(

        Long id,

        Long ticketId,

        String content,

        CommentVisibility visibility,

        Long authorId,

        String authorName,

        Role authorRole,

        LocalDateTime createdAt

) {
}