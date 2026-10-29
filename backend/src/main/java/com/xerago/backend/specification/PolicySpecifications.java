package com.xerago.backend.specification;

import com.xerago.backend.entity.Policy;
import com.xerago.backend.entity.PolicyStatus;
import com.xerago.backend.entity.PolicyType;
import org.springframework.data.jpa.domain.Specification;

public final class PolicySpecifications {

    private PolicySpecifications() {
    }

    public static Specification<Policy> matchesSearch(String search) {
        if (search == null || search.isBlank()) {
            return (root, query, cb) -> cb.conjunction();
        }
        String pattern = "%" + escapeLike(search.trim().toLowerCase()) + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("customerName")), pattern, '\\'),
                cb.like(cb.lower(root.get("policyNumber")), pattern, '\\'));
    }

    public static Specification<Policy> hasStatus(PolicyStatus status) {
        if (status == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Policy> hasType(PolicyType type) {
        if (type == null) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> cb.equal(root.get("policyType"), type);
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
