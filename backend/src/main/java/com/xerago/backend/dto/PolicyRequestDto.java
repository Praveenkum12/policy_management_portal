package com.xerago.backend.dto;

import com.xerago.backend.entity.PolicyStatus;
import com.xerago.backend.entity.PolicyType;
import com.xerago.backend.validation.ValidEnum;
import com.xerago.backend.validation.ValidIsoDate;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PolicyRequestDto(

        @NotBlank(message = "Policy number is required")
        @Pattern(regexp = "\\d*", message = "Policy number must contain only digits")
        @Size(max = 10, message = "Policy number must be at most 10 digits")
        String policyNumber,

        @NotBlank(message = "Customer name is required")
        @Size(max = 120, message = "Customer name must be at most 120 characters")
        String customerName,

        @NotBlank(message = "Policy type is required (Life, Health, Motor or Travel)")
        @ValidEnum(PolicyType.class)
        String policyType,

        @NotNull(message = "Premium amount is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Premium amount must be greater than zero")
        @Digits(integer = 10, fraction = 2, message = "Premium amount allows up to 10 digits and 2 decimals")
        BigDecimal premiumAmount,

        @NotBlank(message = "Status is required (Active, Pending or Cancelled)")
        @ValidEnum(PolicyStatus.class)
        String status,

        @NotBlank(message = "Start date is required")
        @ValidIsoDate
        String startDate) {

    public PolicyRequestDto {
        policyNumber = policyNumber == null ? null : policyNumber.trim();
        customerName = customerName == null ? null : customerName.trim();
        policyType = policyType == null ? null : policyType.trim();
        status = status == null ? null : status.trim();
        startDate = startDate == null ? null : startDate.trim();
    }
}
