package com.xerago.backend.entity;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum PolicyStatus {
    ACTIVE("Active"),
    PENDING("Pending"),
    CANCELLED("Cancelled");

    private final String label;

    PolicyStatus(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }

    public static PolicyStatus from(String value) {
        String input = value.trim();
        return Arrays.stream(values())
                .filter(s -> s.label.equalsIgnoreCase(input) || s.name().equalsIgnoreCase(input))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown status: " + value));
    }
}
