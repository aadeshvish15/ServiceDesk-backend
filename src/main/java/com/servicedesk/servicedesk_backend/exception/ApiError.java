
package com.servicedesk.servicedesk_backend.exception;

import java.time.Instant;
import java.util.Map;

// STEP 1: DEFINE STANDARD API ERROR RESPONSE

// All handled API errors will follow this response structure.
public record ApiError(

        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> validationErrors

) {
}

