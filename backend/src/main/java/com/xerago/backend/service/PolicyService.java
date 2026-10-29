package com.xerago.backend.service;

import com.xerago.backend.dto.PageResponse;
import com.xerago.backend.dto.PolicyRequest;
import com.xerago.backend.dto.PolicyResponse;
import com.xerago.backend.entity.PolicyStatus;

import java.util.List;

public interface PolicyService {

    PageResponse<PolicyResponse> list(String search, PolicyStatus status, int page, int limit, List<String> sort);

    PolicyResponse get(Long id);

    PolicyResponse create(PolicyRequest request);

    PolicyResponse update(Long id, PolicyRequest request);

    void delete(Long id);
}
