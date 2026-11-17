package com.servicedesk.servicedesk_backend.activity.dto;

// STEP 1: IMPORT REQUIRED CLASSES

import com.servicedesk.servicedesk_backend.activity.ActivityAction;
import com.servicedesk.servicedesk_backend.activity.ActivityVisibility;
import com.servicedesk.servicedesk_backend.user.Role;

import java.time.LocalDateTime;


// STEP 2: DEFINE ACTIVITY RESPONSE

public record ActivityResponse(

        Long id,

        Long ticketId,

        ActivityAction action,

        String description,

        ActivityVisibility visibility,

        Long performedById,

        String performedByName,

        Role performedByRole,

        LocalDateTime createdAt

) {
}