package com.servicedesk.servicedesk_backend.notification.dto;

// STEP 1: IMPORT REQUIRED CLASSES

import java.util.List;


// STEP 2: DEFINE PAGINATED NOTIFICATION RESPONSE

public record NotificationPageResponse(

        List<NotificationResponse> content,

        int page,

        int size,

        long totalElements,

        int totalPages,

        boolean first,

        boolean last

) {
}