package com.servicedesk.servicedesk_backend.comment;

// STEP 1: DEFINE COMMENT VISIBILITY

// PUBLIC comments are visible to employees, agents and admins.
// INTERNAL comments are visible only to agents and admins.

public enum CommentVisibility {

    PUBLIC,
    INTERNAL

}