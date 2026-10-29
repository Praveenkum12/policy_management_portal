package com.xerago.backend.dto;

import com.xerago.backend.entity.PolicyStatus;
import com.xerago.backend.entity.PolicyType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PolicyResponse(
        Long id,
        String policyNumber,
        String customerName,
        PolicyType policyType,
        BigDecimal premiumAmount,
        PolicyStatus status,
        LocalDate startDate,
        LocalDateTime createdAt) {
}
