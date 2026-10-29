package com.xerago.backend.repository;

import com.xerago.backend.entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PolicyRepository extends JpaRepository<Policy, Long>, JpaSpecificationExecutor<Policy> {

    boolean existsByPolicyNumberIgnoreCase(String policyNumber);

    boolean existsByPolicyNumberIgnoreCaseAndIdNot(String policyNumber, Long id);
}
