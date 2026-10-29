package com.xerago.backend;

import com.xerago.backend.entity.Policy;
import com.xerago.backend.entity.PolicyStatus;
import com.xerago.backend.entity.PolicyType;
import com.xerago.backend.repository.PolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PolicyApiIntegrationTest {

    private static final String BASE = "/api/policies";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private PolicyRepository repository;

    private Long firstId;

    @BeforeEach
    void seed() {
        repository.deleteAll();
        firstId = save("POL-1001", "Asha Raman", PolicyType.HEALTH, "12500.00", PolicyStatus.ACTIVE, "2026-01-15").getId();
        save("POL-1002", "Karthik Subbiah", PolicyType.MOTOR, "8450.50", PolicyStatus.ACTIVE, "2026-02-01");
        save("POL-1003", "Meera Nair", PolicyType.LIFE, "45000.00", PolicyStatus.PENDING, "2026-11-01");
        save("POL-1004", "Rahul Verma", PolicyType.TRAVEL, "2300.00", PolicyStatus.CANCELLED, "2025-12-10");
        save("POL-1005", "50%_Special", PolicyType.TRAVEL, "100.00", PolicyStatus.ACTIVE, "2026-03-01");
    }

    private Policy save(String number, String name, PolicyType type, String premium, PolicyStatus status, String date) {
        Policy p = new Policy();
        p.setPolicyNumber(number);
        p.setCustomerName(name);
        p.setPolicyType(type);
        p.setPremiumAmount(new BigDecimal(premium));
        p.setStatus(status);
        p.setStartDate(LocalDate.parse(date));
        return repository.saveAndFlush(p);
    }

    private static final String VALID_BODY = """
            {"policyNumber":"2001","customerName":"  New Customer ","policyType":"life",
             "premiumAmount":1999.5,"status":"pending","startDate":"2026-12-01"}""";

    @Test
    void listReturnsPageEnvelopeWithLimit() throws Exception {
        mvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.limit").value(10))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.sort[0]").value("createdAt,desc"));
    }

    @Test
    void listPaginatesAndSorts() throws Exception {
        mvc.perform(get(BASE).param("limit", "2").param("page", "1").param("sort", "customerName,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].customerName").value("Karthik Subbiah"))
                .andExpect(jsonPath("$.content[1].customerName").value("Meera Nair"))
                .andExpect(jsonPath("$.limit").value(2))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.first").value(false))
                .andExpect(jsonPath("$.last").value(false));
    }

    @Test
    void listSortsByPremiumDescending() throws Exception {
        mvc.perform(get(BASE).param("sort", "premiumAmount,desc"))
                .andExpect(jsonPath("$.content[0].policyNumber").value("POL-1003"));
    }

    @Test
    void listSortsWithSeparateDirectionParameter() throws Exception {
        mvc.perform(get(BASE).param("sort", "premiumAmount").param("direction", "desc"))
                .andExpect(jsonPath("$.content[0].policyNumber").value("POL-1003"))
                .andExpect(jsonPath("$.sort[0]").value("premiumAmount,desc"));
        mvc.perform(get(BASE).param("sort", "premiumAmount").param("direction", "ASC"))
                .andExpect(jsonPath("$.content[0].policyNumber").value("POL-1005"));
        mvc.perform(get(BASE).param("sort", "premiumAmount"))
                .andExpect(jsonPath("$.sort[0]").value("premiumAmount,asc"));
        mvc.perform(get(BASE).param("direction", "asc"))
                .andExpect(jsonPath("$.sort[0]").value("createdAt,asc"));
        mvc.perform(get(BASE).param("sort", "premiumAmount").param("direction", "up"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("asc, desc")));
    }

    @Test
    void listPastLastPageIsEmptyWithRealTotals() throws Exception {
        mvc.perform(get(BASE).param("page", "9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(5));
    }

    @Test
    void listSearchesCustomerNameAndPolicyNumberIgnoringCase() throws Exception {
        mvc.perform(get(BASE).param("search", "asha"))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].policyNumber").value("POL-1001"));
        mvc.perform(get(BASE).param("search", "pol-1003"))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].customerName").value("Meera Nair"));
    }

    @Test
    void listTreatsLikeWildcardsAsLiterals() throws Exception {
        mvc.perform(get(BASE).param("search", "%"))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].policyNumber").value("POL-1005"));
        mvc.perform(get(BASE).param("search", "_"))
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void listFiltersByStatusCaseInsensitivelyAndCombinesWithSearch() throws Exception {
        mvc.perform(get(BASE).param("status", "active"))
                .andExpect(jsonPath("$.totalElements").value(3));
        mvc.perform(get(BASE).param("status", "ACTIVE").param("search", "karthik"))
                .andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(get(BASE).param("status", "Cancelled").param("search", "asha"))
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void listFiltersByPolicyTypeAndCombinesWithOtherFilters() throws Exception {
        mvc.perform(get(BASE).param("policyType", "travel"))
                .andExpect(jsonPath("$.totalElements").value(2));
        mvc.perform(get(BASE).param("policyType", "Health"))
                .andExpect(jsonPath("$.content[0].policyNumber").value("POL-1001"))
                .andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(get(BASE).param("policyType", "travel").param("status", "active"))
                .andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(get(BASE).param("policyType", "boat"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].message", containsString("Life, Health, Motor, Travel")));
    }

    @Test
    void listRejectsInvalidParameters() throws Exception {
        mvc.perform(get(BASE).param("limit", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("limit"));
        mvc.perform(get(BASE).param("limit", "0")).andExpect(status().isBadRequest());
        mvc.perform(get(BASE).param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("page"));
        mvc.perform(get(BASE).param("page", "abc")).andExpect(status().isBadRequest());
        mvc.perform(get(BASE).param("sort", "password,desc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Allowed fields")));
        mvc.perform(get(BASE).param("status", "foo"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].message", containsString("Active, Pending, Cancelled")));
    }

    @Test
    void getReturnsPolicyOr404() throws Exception {
        mvc.perform(get(BASE + "/" + firstId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(firstId))
                .andExpect(jsonPath("$.policyType").value("Health"))
                .andExpect(jsonPath("$.status").value("Active"))
                .andExpect(jsonPath("$.premiumAmount").value(12500.00))
                .andExpect(jsonPath("$.createdAt").exists());
        mvc.perform(get(BASE + "/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value(BASE + "/999999"));
        mvc.perform(get(BASE + "/abc")).andExpect(status().isBadRequest());
    }

    @Test
    void createReturns201WithLocationAndNormalizedValues() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString(BASE + "/")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.policyNumber").value("POL-2001"))
                .andExpect(jsonPath("$.customerName").value("New Customer"))
                .andExpect(jsonPath("$.policyType").value("Life"))
                .andExpect(jsonPath("$.status").value("Pending"))
                .andExpect(jsonPath("$.premiumAmount").value(1999.50));
    }

    @Test
    void createRejectsDuplicatePolicyNumberCaseInsensitively() throws Exception {
        String body = VALID_BODY.replace("2001", "1001");
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("policyNumber"));
    }

    @Test
    void createReportsAllValidationErrors() throws Exception {
        String body = """
                {"policyNumber":" ","customerName":"","premiumAmount":0}""";
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors[?(@.field=='policyNumber')]").exists())
                .andExpect(jsonPath("$.fieldErrors[?(@.field=='customerName')]").exists())
                .andExpect(jsonPath("$.fieldErrors[?(@.field=='policyType')]").exists())
                .andExpect(jsonPath("$.fieldErrors[?(@.field=='premiumAmount')]").exists())
                .andExpect(jsonPath("$.fieldErrors[?(@.field=='status')]").exists())
                .andExpect(jsonPath("$.fieldErrors[?(@.field=='startDate')]").exists());
    }

    @Test
    void createRejectsNegativePremiumInvalidStatusAndInvalidDate() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY.replace("1999.5", "-5")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("premiumAmount"));
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY.replace("\"pending\"", "\"Expired\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("status"))
                .andExpect(jsonPath("$.fieldErrors[0].message", containsString("Allowed values")));
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY.replace("2026-12-01", "2026-13-45")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("startDate"));
    }

    @Test
    void createReportsInvalidEnumsDateAndPremiumTogether() throws Exception {
        String body = """
                {"policyNumber":"9","customerName":"X","policyType":"car",
                 "premiumAmount":-1,"status":"Expired","startDate":"2026-13-45"}""";
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors", hasSize(4)))
                .andExpect(jsonPath("$.fieldErrors[?(@.field=='policyType')].message",
                        hasItem(containsString("Allowed values: Life, Health, Motor, Travel"))))
                .andExpect(jsonPath("$.fieldErrors[?(@.field=='status')].message",
                        hasItem(containsString("Allowed values: Active, Pending, Cancelled"))))
                .andExpect(jsonPath("$.fieldErrors[?(@.field=='startDate')]").exists())
                .andExpect(jsonPath("$.fieldErrors[?(@.field=='premiumAmount')]").exists());
    }

    @Test
    void createRejectsNonNumericOrTooLongPolicyNumber() throws Exception {
        for (String bad : new String[]{"POL-2001", "12ab", "12345678901", "-5", "1 2"}) {
            mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_BODY.replace("\"2001\"", "\"" + bad + "\"")))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors[0].field").value("policyNumber"));
        }
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY.replace("2001", "1234567890")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.policyNumber").value("POL-1234567890"));
    }

    @Test
    void createRejectsMalformedJson() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content("{oops"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void updateKeepsIdAndAllowsSamePolicyNumber() throws Exception {
        String body = VALID_BODY.replace("2001", "1001");
        mvc.perform(put(BASE + "/" + firstId).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(firstId))
                .andExpect(jsonPath("$.customerName").value("New Customer"));
        mvc.perform(get(BASE + "/" + firstId))
                .andExpect(jsonPath("$.status").value("Pending"));
    }

    @Test
    void updateIgnoresIdInBody() throws Exception {
        String body = VALID_BODY.replace("{", "{\"id\":424242,");
        mvc.perform(put(BASE + "/" + firstId).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(firstId));
    }

    @Test
    void updateReturns404WhenMissingAnd409OnDuplicate() throws Exception {
        mvc.perform(put(BASE + "/999999").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isNotFound());
        mvc.perform(put(BASE + "/" + firstId).contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY.replace("2001", "1002")))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteReturns204ThenSecondDeleteReturns404() throws Exception {
        mvc.perform(delete(BASE + "/" + firstId)).andExpect(status().isNoContent());
        mvc.perform(get(BASE + "/" + firstId)).andExpect(status().isNotFound());
        mvc.perform(delete(BASE + "/" + firstId)).andExpect(status().isNotFound());
    }

    @Test
    void unsupportedMethodAndUnknownRouteUseSameErrorShape() throws Exception {
        mvc.perform(delete(BASE))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.timestamp").exists());
        mvc.perform(get("/api/nothing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void corsAllowsConfiguredOrigin() throws Exception {
        mvc.perform(get(BASE).header("Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }
}
