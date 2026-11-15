
package com.servicedesk.servicedesk_backend.category.exception;

// STEP 1: DEFINE CUSTOM CATEGORY EXCEPTION

// Thrown when an admin tries to create or rename a category
// using a name that already exists.
public class CategoryAlreadyExistsException extends RuntimeException {

    public CategoryAlreadyExistsException(String message) {

        super(message);
    }
}
