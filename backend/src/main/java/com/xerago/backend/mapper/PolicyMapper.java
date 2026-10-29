package com.xerago.backend.mapper;

import com.xerago.backend.dto.PolicyRequest;
import com.xerago.backend.dto.PolicyResponse;
import com.xerago.backend.entity.Policy;

public final class PolicyMapper {

    private PolicyMapper() {
    }

    public static Policy toEntity(PolicyRequest request) {
        Policy policy = new Policy();
        apply(policy, request);
        return policy;
    }

    public static void apply(Policy policy, PolicyRequest request) {
        policy.setPolicyNumber(request.policyNumber());
        policy.setCustomerName(request.customerName());
        policy.setPolicyType(request.policyType());
        policy.setPremiumAmount(request.premiumAmount().setScale(2));
        policy.setStatus(request.status());
        policy.setStartDate(request.startDate());
    }

    public static PolicyResponse toResponse(Policy policy) {
        return new PolicyResponse(
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
