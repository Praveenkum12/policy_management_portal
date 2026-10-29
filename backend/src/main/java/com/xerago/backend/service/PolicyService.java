package com.xerago.backend.service;

import com.xerago.backend.dto.PageResponseDto;
import com.xerago.backend.dto.PolicyRequestDto;
import com.xerago.backend.dto.PolicyResponseDto;
import com.xerago.backend.entity.PolicyStatus;

import java.util.List;

public interface PolicyService {

    PageResponseDto<PolicyResponseDto> list(String search, PolicyStatus status, int page, int limit, List<String> sort,
                                      String direction);

    PolicyResponseDto get(Long id);

    PolicyResponseDto create(PolicyRequestDto request);

    PolicyResponseDto update(Long id, PolicyRequestDto request);

    void delete(Long id);
}
