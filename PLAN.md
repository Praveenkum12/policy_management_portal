# Policy Management Portal: Implementation Plan

Xerago practical development assessment. **Due: 29.10.26**, by email reply.

- Frontend: React (Vite)
- Backend: Spring Boot 4.1.2, Java 25 (already scaffolded in `backend/`)
- Database: MySQL (primary, already configured), H2 for tests

---

## 1. Current state

| Item | Status |
|---|---|
| `backend/` Spring Boot 4.1.2, Java 25, Maven wrapper | Done |
| Dependencies: `data-jpa`, `webmvc`, `devtools`, `mysql-connector-j`, `lombok`, test starters | Done |
| Package `com.xerago.backend`, server port `8000` | Done |
| MySQL config: `localhost:3308`, DB `jobportal`, root/root | Needs a check (see Open items) |
| `spring-boot-starter-validation` | **Missing**, must be added |
| H2 for tests | **Missing**, must be added (test scope) |
| `ddl-auto` / dialect settings | **Missing** |
| `frontend/` | Not started |

---

## 2. Requirements checklist

| # | Requirement | Where it's covered |
|---|---|---|
| 1 | Listing: Policy No, Customer, Type, Premium, Status, Start Date | `PolicyTable` |
| 2 | Create: required fields, premium > 0, valid status, valid date | `PolicyForm` + Bean Validation |
| 3 | Edit: ID unchanged | `PUT /api/policies/{id}`, ID is never read from the body |
| 4 | Delete with confirmation | `ConfirmDialog` |
| 5 | Search by customer or policy number, filter by status | `search` and `status` query params |
| 6 | React calls the REST API, with no data only in state | `api/policyApi.js` |
| 7 | Persistence via JPA, survives refresh | MySQL |
| 8 | Messages for validation, API errors, empty results, loading | UI states + error response shape |

---

## 3. Backend design

### Package layout (`com.xerago.backend`)
```
controller/   PolicyController
service/      PolicyService (interface or class), PolicyServiceImpl
repository/   PolicyRepository (JpaRepository + JpaSpecificationExecutor)
entity/       Policy, PolicyStatus (enum), PolicyType (enum)
dto/          PolicyRequest, PolicyResponse, ErrorResponse
exception/    PolicyNotFoundException, DuplicatePolicyException, GlobalExceptionHandler
config/       CorsConfig, DataSeeder
```

### Entity `Policy`
| Field | Type | Constraints |
|---|---|---|
| id | Long | `@GeneratedValue(IDENTITY)` |
| policyNumber | String | not null, **unique** |
| customerName | String | not null |
| policyType | enum `LIFE/HEALTH/MOTOR/TRAVEL` | not null |
| premiumAmount | BigDecimal(12,2) | not null, > 0 |
| status | enum `ACTIVE/PENDING/CANCELLED` | not null, stored `EnumType.STRING` |
| startDate | LocalDate | not null |
| createdAt | LocalDateTime | `@CreationTimestamp`, not updatable |

### DTO validation (`PolicyRequest`)
- `@NotBlank` + `@Size` on `policyNumber` and `customerName`
- `@NotNull` on `policyType`
- `@NotNull` + `@DecimalMin(value="0.0", inclusive=false)` + `@Digits` on `premiumAmount`
- `@NotNull` on `status`. An invalid enum value gives 400 via the `HttpMessageNotReadableException` handler.
- `@NotNull` on `startDate`. A malformed date gives 400 from the same handler.

### REST API
| Method | Endpoint | Success | Errors |
|---|---|---|---|
| GET | `/api/policies?search=&status=` | 200, array | 400 on an invalid status |
| GET | `/api/policies/{id}` | 200 | 404 |
| POST | `/api/policies` | **201** + `Location` header | 400 validation, 409 duplicate number |
| PUT | `/api/policies/{id}` | 200 | 400, 404, 409 |
| DELETE | `/api/policies/{id}` | **204** | 404 |

### Consistent error body
```json
{
  "timestamp": "2026-10-29T10:15:30",
  "status": 400,
  "error": "Validation Failed",
  "message": "One or more fields are invalid",
  "path": "/api/policies",
  "fieldErrors": { "premiumAmount": "must be greater than zero" }
}
```
`GlobalExceptionHandler` (`@RestControllerAdvice`) maps:
- `MethodArgumentNotValidException` → 400
- `HttpMessageNotReadableException` → 400
- `MethodArgumentTypeMismatchException` → 400
- `PolicyNotFoundException` → 404
- `DuplicatePolicyException` and `DataIntegrityViolationException` → 409
- `Exception` → 500, with no stack trace leaked

### Search and filter
Specification-based query, which is easier to combine with pagination later:
`(lower(customerName) LIKE %q% OR lower(policyNumber) LIKE %q%) AND status = ?`. Both parameters are optional.

### Config changes needed in `pom.xml` and `application.properties`
- Add `spring-boot-starter-validation`.
- Add `h2` (test scope) for repository and service tests.
- Set `spring.jpa.hibernate.ddl-auto=update`, so tables are created on first run. This is a documented assumption.
- Set `spring.jpa.open-in-view=false`.
- Keep the env-var-driven datasource settings.

### Seed data
`DataSeeder` (`CommandLineRunner`) inserts about 8 sample policies **only if the table is empty**. This is initial data and not a data source, which satisfies "avoid hardcoding sample records as the only data source".

### Spring Boot 4 notes (to avoid surprises)
- Starters are modular. The web starter is `spring-boot-starter-webmvc`, already in place.
- Jackson 3 (`tools.jackson`) is the default.
- Test slice annotations moved to new packages (e.g. `@WebMvcTest` now lives under `org.springframework.boot.webmvc.test.autoconfigure`). I'll verify these at compile time.

---

## 4. Frontend design (`frontend/`, React + Vite, JavaScript)

```
src/
  api/policyApi.js        fetch wrapper: list, get, create, update, remove, normalized errors
  hooks/usePolicies.js    list state, loading, error, search/status filters, refetch
  hooks/useDebounce.js    300ms debounce for the search box
  components/
    PolicyTable.jsx       table (cards on mobile)
    PolicyForm.jsx        shared create/edit form, inline field errors
    SearchFilterBar.jsx   search input + status dropdown + "Add policy" button
    ConfirmDialog.jsx     delete confirmation
    StatusBadge.jsx       colored Active / Pending / Cancelled
    Feedback.jsx          Spinner, EmptyState, ErrorBanner, Toast
  App.jsx                 composes list + modal form
  index.css               plain responsive CSS
```

- Dev proxy in `vite.config.js`: `/api` → `http://localhost:8000`. No CORS problems in dev, and `CorsConfig` is still added for other setups.
- Client-side checks mirror the server rules for fast feedback. The server is the source of truth and its `fieldErrors` are shown next to the fields.
- UI states: loading spinner, "No policies match your search", error banner with Retry, success toasts, submit button disabled while saving.
- No heavy UI library. Plain CSS keeps it small and easy to explain.

---

## 5. Build order

1. **Backend core**: add dependencies, entity, enums, repository, DTOs, service, controller, exception handler, seeder. Run and test with curl.
2. **Backend tests**: service unit tests (Mockito), controller tests (`@WebMvcTest`), repository search test (H2).
3. **Frontend**: Vite scaffold, API layer, hook, components, styling. Test against the running backend.
4. **Bonus, if time allows**: pagination and sorting (`Pageable`, `Page<T>`; server-side sort by column header).
5. **Docker Compose**: MySQL + backend (multi-stage Dockerfile) + frontend (nginx serving the build and proxying `/api`).
6. **Docs**: README, screenshots, `WALKTHROUGH.md`.

JWT and role-based access are **out of scope**. The assessment says to attempt them only once the core is complete, and a rushed implementation would add risk.

---

## 6. Deliverables

```
policy_management_portal/
├── backend/
├── frontend/
├── docker-compose.yml        (bonus)
├── README.md                 prerequisites, run commands, DB setup, API summary, assumptions
├── WALKTHROUGH.md            architecture, validation approach, one design trade-off
├── PLAN.md                   this file
└── screenshots/              list, create, edit, delete confirm, search, validation errors, empty state
```

### Walkthrough talking points
- **Architecture:** controller → service → repository. DTOs separate the API contract from the entity, so clients can't set `id` or `createdAt`.
- **Validation:** two layers. Bean Validation at the API boundary, plus a DB unique constraint that backs up the duplicate check under concurrency.
- **Trade-off (candidate):** server-side filtering with a Specification instead of client-side filtering. It costs a request per change (mitigated by debouncing) but scales, and it works with pagination.
- **Trade-off (alternative):** `ddl-auto=update` for convenience instead of Flyway migrations. It's fine for an assessment, but production needs migrations.

---

## 7. Open items to confirm

1. **MySQL**: the config points to `localhost:3308`, DB `jobportal`, root/root. Is a MySQL instance running there? Should I rename the DB to `policy_db`? (`createDatabaseIfNotExist=true` can be added to the URL.)
2. **Pagination and sorting**: plain array first, with pagination as a late bonus (recommended), or built in from the start?
3. **Frontend language**: JavaScript (recommended for speed) or TypeScript?
4. **Bonus scope**: tests + Docker Compose, skipping JWT (recommended)?
5. **Package name**: keep `com.xerago.backend`?
