package com.xerago.backend.exception;

public class DuplicatePolicyException extends RuntimeException {

    public DuplicatePolicyException(String policyNumber) {
        super("Policy number '" + policyNumber + "' already exists.");
    }
}
