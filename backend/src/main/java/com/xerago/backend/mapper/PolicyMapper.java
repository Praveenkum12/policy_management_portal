package com.xerago.backend.mapper;

import com.xerago.backend.dto.PolicyRequestDto;
import com.xerago.backend.dto.PolicyResponseDto;
import com.xerago.backend.entity.Policy;

public final class PolicyMapper {

    private PolicyMapper() {
    }

    public static Policy toEntity(PolicyRequestDto request) {
        Policy policy = new Policy();
        apply(policy, request);
        return policy;
    }

    public static void apply(Policy policy, PolicyRequestDto request) {
        policy.setPolicyNumber(request.policyNumber());
        policy.setCustomerName(request.customerName());
        policy.setPolicyType(request.policyType());
        policy.setPremiumAmount(request.premiumAmount().setScale(2));
        policy.setStatus(request.status());
        policy.setStartDate(request.startDate());
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
