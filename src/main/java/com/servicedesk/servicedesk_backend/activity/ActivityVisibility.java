package com.servicedesk.servicedesk_backend.activity;

// STEP 1: DEFINE ACTIVITY VISIBILITY

// PUBLIC events are visible to employees.
// INTERNAL events are visible only to agents and admins.

public enum ActivityVisibility {

    PUBLIC,
    INTERNAL

}