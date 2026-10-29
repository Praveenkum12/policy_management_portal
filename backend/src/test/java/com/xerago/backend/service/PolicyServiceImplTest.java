package com.xerago.backend.service;

import com.xerago.backend.dto.PolicyRequestDto;
import com.xerago.backend.dto.PolicyResponseDto;
import com.xerago.backend.entity.Policy;
import com.xerago.backend.entity.PolicyStatus;
import com.xerago.backend.entity.PolicyType;
import com.xerago.backend.exception.DuplicatePolicyException;
import com.xerago.backend.exception.InvalidSortException;
import com.xerago.backend.exception.PolicyNotFoundException;
import com.xerago.backend.repository.PolicyRepository;
import com.xerago.backend.service.impl.PolicyServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PolicyServiceImplTest {

    @Mock
    private PolicyRepository repository;

    @InjectMocks
    private PolicyServiceImpl service;

    private PolicyRequestDto request(String number) {
        return new PolicyRequestDto(number, "Asha Raman", "Health", new BigDecimal("1500"),
                "Active", "2026-01-01");
    }

    private Policy stored(Long id, String number) {
        Policy policy = new Policy();
        policy.setId(id);
        policy.setPolicyNumber(number);
        policy.setCustomerName("Asha Raman");
        policy.setPolicyType(PolicyType.HEALTH);
        policy.setPremiumAmount(new BigDecimal("1500.00"));
        policy.setStatus(PolicyStatus.ACTIVE);
        policy.setStartDate(LocalDate.of(2026, 1, 1));
        return policy;
    }

    @Test
    void createSavesNormalizedPolicy() {
        when(repository.existsByPolicyNumberIgnoreCase("POL-1")).thenReturn(false);
        when(repository.saveAndFlush(any(Policy.class))).thenAnswer(inv -> {
            Policy p = inv.getArgument(0);
            p.setId(7L);
            return p;
        });

        PolicyResponseDto response = service.create(request(" 1 "));

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.policyNumber()).isEqualTo("POL-1");
        assertThat(response.premiumAmount()).isEqualByComparingTo("1500.00");
        assertThat(response.premiumAmount().scale()).isEqualTo(2);
    }

    @Test
    void createRejectsDuplicatePolicyNumber() {
        when(repository.existsByPolicyNumberIgnoreCase("POL-1")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request("1"))).isInstanceOf(DuplicatePolicyException.class);
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void getThrowsWhenMissing() {
        when(repository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(9L)).isInstanceOf(PolicyNotFoundException.class);
    }

    @Test
    void updateThrowsWhenMissing() {
        when(repository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(9L, request("1"))).isInstanceOf(PolicyNotFoundException.class);
    }

    @Test
    void updateKeepsIdAndAllowsOwnPolicyNumber() {
        Policy existing = stored(3L, "POL-1");
        when(repository.findById(3L)).thenReturn(Optional.of(existing));
        when(repository.existsByPolicyNumberIgnoreCaseAndIdNot("POL-1", 3L)).thenReturn(false);
        when(repository.saveAndFlush(existing)).thenReturn(existing);

        PolicyResponseDto response = service.update(3L, new PolicyRequestDto("1", "New Name", "life",
                new BigDecimal("99.5"), "PENDING", "2027-02-02"));

        assertThat(response.id()).isEqualTo(3L);
        assertThat(response.customerName()).isEqualTo("New Name");
        assertThat(response.status()).isEqualTo(PolicyStatus.PENDING);
    }

    @Test
    void updateRejectsPolicyNumberOwnedByAnotherPolicy() {
        when(repository.findById(3L)).thenReturn(Optional.of(stored(3L, "POL-1")));
        when(repository.existsByPolicyNumberIgnoreCaseAndIdNot("POL-2", 3L)).thenReturn(true);

        assertThatThrownBy(() -> service.update(3L, request("2"))).isInstanceOf(DuplicatePolicyException.class);
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void deleteThrowsWhenMissing() {
        when(repository.existsById(5L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(5L)).isInstanceOf(PolicyNotFoundException.class);
        verify(repository, never()).deleteById(any());
    }

    @Test
    void deleteRemovesExistingPolicy() {
        when(repository.existsById(5L)).thenReturn(true);

        service.delete(5L);

        verify(repository).deleteById(5L);
    }

    @Test
    @SuppressWarnings("unchecked")
    void listUsesDefaultSortAndAddsIdTieBreaker() {
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(stored(1L, "POL-1"))));
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);

        var page = service.list(null, null, null, 0, 10, null, null);

        verify(repository).findAll(any(Specification.class), pageable.capture());
        assertThat(pageable.getValue().getSort().toString()).isEqualTo("createdAt: DESC,id: ASC");
        assertThat(page.sort()).containsExactly("createdAt,desc");
        assertThat(page.content()).hasSize(1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void listParsesMultipleSortFields() {
        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        var page = service.list(null, null, null, 0, 10, List.of("status", "desc", "customerName"), null);

        assertThat(page.sort()).containsExactly("status,desc", "customerName,asc");
    }

    @Test
    @SuppressWarnings("unchecked")
    void listAppliesDirectionParameter() {
        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        assertThat(service.list(null, null, null, 0, 10, List.of("customerName"), "desc").sort())
                .containsExactly("customerName,desc");
        assertThat(service.list(null, null, null, 0, 10, null, "asc").sort())
                .containsExactly("createdAt,asc");
        assertThat(service.list(null, null, null, 0, 10, List.of("status,asc", "customerName"), "DESC").sort())
                .containsExactly("status,asc", "customerName,desc");
    }

    @Test
    void listRejectsInvalidDirection() {
        assertThatThrownBy(() -> service.list(null, null, null, 0, 10, null, "up"))
                .isInstanceOf(InvalidSortException.class)
                .hasMessageContaining("asc, desc");
    }

    @Test
    void listRejectsUnknownSortField() {
        assertThatThrownBy(() -> service.list(null, null, null, 0, 10, List.of("password,desc"), null))
                .isInstanceOf(InvalidSortException.class)
                .hasMessageContaining("password");
    }
}
