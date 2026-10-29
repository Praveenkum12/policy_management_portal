package com.xerago.backend.controller;

import com.xerago.backend.dto.PageResponseDto;
import com.xerago.backend.dto.PolicyRequestDto;
import com.xerago.backend.dto.PolicyResponseDto;
import com.xerago.backend.entity.PolicyStatus;
import com.xerago.backend.entity.PolicyType;
import com.xerago.backend.service.PolicyService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/policies")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;

    @GetMapping(version = "1.0")
    public ResponseEntity<PageResponseDto<PolicyResponseDto>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) PolicyStatus status,
            @RequestParam(required = false) PolicyType policyType,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page must be 0 or greater") int page,
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "Limit must be at least 1")
            @Max(value = 50, message = "Limit must be at most 50") int limit,
            @RequestParam(required = false) List<String> sort,
            @RequestParam(required = false) String direction) {
        return ResponseEntity.ok(policyService.list(search, status, policyType, page, limit, sort, direction));
    }

    @GetMapping(path = "/{id}", version = "1.0")
    public ResponseEntity<PolicyResponseDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(policyService.get(id));
    }

    @PostMapping(version = "1.0")
    public ResponseEntity<PolicyResponseDto> create(@Valid @RequestBody PolicyRequestDto request) {
        PolicyResponseDto created = policyService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping(path = "/{id}", version = "1.0")
    public ResponseEntity<PolicyResponseDto> update(@PathVariable Long id, @Valid @RequestBody PolicyRequestDto request) {
        return ResponseEntity.ok(policyService.update(id, request));
    }

    @DeleteMapping(path = "/{id}", version = "1.0")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        policyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
