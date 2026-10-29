package com.xerago.backend.dto;

import com.xerago.backend.entity.PolicyStatus;
import com.xerago.backend.entity.PolicyType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PolicyRequest(

        @NotBlank(message = "Policy number is required")
        @Size(max = 50, message = "Policy number must be at most 50 characters")
        @Pattern(regexp = "[A-Za-z0-9\\-_/]*", message = "Policy number may contain only letters, digits, '-', '_' and '/'")
        String policyNumber,

        @NotBlank(message = "Customer name is required")
        @Size(max = 120, message = "Customer name must be at most 120 characters")
        String customerName,

        @NotNull(message = "Policy type is required (Life, Health, Motor or Travel)")
        PolicyType policyType,

        @NotNull(message = "Premium amount is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Premium amount must be greater than zero")
        @Digits(integer = 10, fraction = 2, message = "Premium amount allows up to 10 digits and 2 decimals")
        BigDecimal premiumAmount,

        @NotNull(message = "Status is required (Active, Pending or Cancelled)")
        PolicyStatus status,

        @NotNull(message = "Start date is required")
        LocalDate startDate) {

    public PolicyRequest {
        policyNumber = policyNumber == null ? null : policyNumber.trim().toUpperCase();
        customerName = customerName == null ? null : customerName.trim();
    }
}
