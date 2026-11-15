
package com.servicedesk.servicedesk_backend.category.exception;

// STEP 1: DEFINE RESOURCE NOT FOUND EXCEPTION

// Used when a requested database resource does not exist.
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {

        super(message);
    }
}
