package com.xerago.backend.service.impl;

import com.xerago.backend.dto.PageResponseDto;
import com.xerago.backend.dto.PolicyRequestDto;
import com.xerago.backend.dto.PolicyResponseDto;
import com.xerago.backend.entity.Policy;
import com.xerago.backend.entity.PolicyStatus;
import com.xerago.backend.exception.DuplicatePolicyException;
import com.xerago.backend.exception.InvalidSortException;
import com.xerago.backend.exception.PolicyNotFoundException;
import com.xerago.backend.mapper.PolicyMapper;
import com.xerago.backend.repository.PolicyRepository;
import com.xerago.backend.service.PolicyService;
import com.xerago.backend.specification.PolicySpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PolicyServiceImpl implements PolicyService {

    static final Set<String> SORTABLE_FIELDS = Set.of(
            "policyNumber", "customerName", "policyType", "premiumAmount", "status", "startDate", "createdAt");

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "createdAt");

    private final PolicyRepository repository;

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<PolicyResponseDto> list(String search, PolicyStatus status, int page, int limit,
                                                List<String> sort) {
        Sort requested = parseSort(sort);
        Specification<Policy> spec = PolicySpecifications.matchesSearch(search)
                .and(PolicySpecifications.hasStatus(status));

        Page<Policy> result = repository.findAll(spec,
                PageRequest.of(page, limit, requested.and(Sort.by(Sort.Direction.ASC, "id"))));

        return new PageResponseDto<>(
                result.getContent().stream().map(PolicyMapper::toResponse).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isFirst(),
                result.isLast(),
                requested.stream().map(o -> o.getProperty() + "," + o.getDirection().name().toLowerCase()).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PolicyResponseDto get(Long id) {
        return PolicyMapper.toResponse(findOrThrow(id));
    }

    @Override
    @Transactional
    public PolicyResponseDto create(PolicyRequestDto request) {
        if (repository.existsByPolicyNumberIgnoreCase(request.policyNumber())) {
            throw new DuplicatePolicyException(request.policyNumber());
        }
        Policy saved = repository.saveAndFlush(PolicyMapper.toEntity(request));
        return PolicyMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public PolicyResponseDto update(Long id, PolicyRequestDto request) {
        Policy policy = findOrThrow(id);
        if (repository.existsByPolicyNumberIgnoreCaseAndIdNot(request.policyNumber(), id)) {
            throw new DuplicatePolicyException(request.policyNumber());
        }
        PolicyMapper.apply(policy, request);
        return PolicyMapper.toResponse(repository.saveAndFlush(policy));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new PolicyNotFoundException(id);
        }
        repository.deleteById(id);
    }

    private Policy findOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() -> new PolicyNotFoundException(id));
    }

    private Sort parseSort(List<String> sort) {
        if (sort == null || sort.isEmpty()) {
            return DEFAULT_SORT;
        }
        List<Sort.Order> orders = new ArrayList<>();
        for (String raw : sort) {
            for (String token : Arrays.stream(raw.split(",")).map(String::trim).filter(t -> !t.isEmpty()).toList()) {
                if (token.equalsIgnoreCase("asc") || token.equalsIgnoreCase("desc")) {
                    if (orders.isEmpty()) {
                        throw new InvalidSortException(token, SORTABLE_FIELDS.stream().sorted().toList());
                    }
                    Sort.Order last = orders.removeLast();
                    orders.add(new Sort.Order(Sort.Direction.fromString(token), last.getProperty()));
                } else if (SORTABLE_FIELDS.contains(token)) {
                    orders.add(Sort.Order.asc(token));
                } else {
                    throw new InvalidSortException(token, SORTABLE_FIELDS.stream().sorted().toList());
                }
            }
        }
        return orders.isEmpty() ? DEFAULT_SORT : Sort.by(orders);
    }
}
