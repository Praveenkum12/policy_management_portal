package com.xerago.backend.mapper;

import com.xerago.backend.dto.PolicyRequestDto;
import com.xerago.backend.dto.PolicyResponseDto;
import com.xerago.backend.entity.Policy;
import com.xerago.backend.entity.PolicyStatus;
import com.xerago.backend.entity.PolicyType;

import java.time.LocalDate;

public final class PolicyMapper {

    public static final String POLICY_NUMBER_PREFIX = "POL-";

    private PolicyMapper() {
    }

    public static String storedPolicyNumber(PolicyRequestDto request) {
        return POLICY_NUMBER_PREFIX + request.policyNumber();
    }

    public static Policy toEntity(PolicyRequestDto request) {
        Policy policy = new Policy();
        apply(policy, request);
        return policy;
    }

    public static void apply(Policy policy, PolicyRequestDto request) {
        policy.setPolicyNumber(storedPolicyNumber(request));
        policy.setCustomerName(request.customerName());
        policy.setPolicyType(PolicyType.from(request.policyType()));
        policy.setPremiumAmount(request.premiumAmount().setScale(2));
        policy.setStatus(PolicyStatus.from(request.status()));
        policy.setStartDate(LocalDate.parse(request.startDate()));
    }

    public static PolicyResponseDto toResponse(Policy policy) {
        return new PolicyResponseDto(
                policy.getId(),
                policy.getPolicyNumber(),
                policy.getCustomerName(),
                policy.getPolicyType(),
                policy.getPremiumAmount(),
                policy.getStatus(),
                policy.getStartDate(),
                policy.getCreatedAt());
    }
}
