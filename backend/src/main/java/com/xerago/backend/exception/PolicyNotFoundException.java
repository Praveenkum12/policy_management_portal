package com.xerago.backend.exception;

public class PolicyNotFoundException extends RuntimeException {

    public PolicyNotFoundException(Long id) {
        super("Policy with id " + id + " was not found.");
    }
}
