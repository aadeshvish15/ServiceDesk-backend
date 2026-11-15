
package com.servicedesk.servicedesk_backend.exception;

// STEP 1: CREATE CUSTOM EXCEPTION

// This exception is thrown when someone tries to
// register using an email that already exists.
public class EmailAlreadyExistsException extends RuntimeException {

    // STEP 2: DEFINE CONSTRUCTOR

    public EmailAlreadyExistsException(String message) {

        // Pass the error message to the parent RuntimeException.
        super(message);
    }
}
