package com.xerago.backend.entity;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum PolicyType {
    LIFE("Life"),
    HEALTH("Health"),
    MOTOR("Motor"),
    TRAVEL("Travel");

    private final String label;

    PolicyType(String label) {
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

    public static PolicyType from(String value) {
        String input = value.trim();
        return Arrays.stream(values())
                .filter(t -> t.label.equalsIgnoreCase(input) || t.name().equalsIgnoreCase(input))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown policy type: " + value));
    }
}
