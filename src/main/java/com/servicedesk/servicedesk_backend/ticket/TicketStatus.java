package com.servicedesk.servicedesk_backend.ticket;

// STEP 1: DEFINE AVAILABLE TICKET STATUSES
// These values represent the lifecycle of a support ticket.

public enum TicketStatus {

    OPEN,
    IN_PROGRESS,
    PENDING,
    RESOLVED,
    CLOSED

}