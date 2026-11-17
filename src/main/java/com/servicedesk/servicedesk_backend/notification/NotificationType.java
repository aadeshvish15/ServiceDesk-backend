package com.servicedesk.servicedesk_backend.notification;

// STEP 1: DEFINE POSSIBLE NOTIFICATION TYPES

public enum NotificationType {

    // STEP 1.1: TICKET ASSIGNMENT EVENTS
    TICKET_ASSIGNED,

    TICKET_REASSIGNED,

    // STEP 1.2: TICKET STATUS EVENT
    TICKET_STATUS_CHANGED,

    // STEP 1.3: COMMENT EVENT
    COMMENT_ADDED
}