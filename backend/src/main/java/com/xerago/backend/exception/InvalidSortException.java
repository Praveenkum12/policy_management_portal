package com.xerago.backend.exception;

import java.util.Collection;

public class InvalidSortException extends RuntimeException {

    public InvalidSortException(String message) {
        super(message);
    }

    public InvalidSortException(String field, Collection<String> allowedFields) {
        super("Cannot sort by '" + field + "'. Allowed fields: " + String.join(", ", allowedFields) + ".");
    }
}
