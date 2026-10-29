# Backend Plan: Policy Management Portal

Spring Boot 4.1.2, Java 25, Maven, Spring Data JPA, MySQL (Docker). The frontend comes after the backend is finished and verified.

---

## 1. Scope and order of work

| Step | What | Done when |
|---|---|---|
| 1 | Docker Compose for MySQL | `docker compose up -d` gives a healthy MySQL on port 3308 |
| 2 | Dependencies and config | App starts and connects to MySQL, tables are auto-created |
| 3 | Domain: entity, enums, repository | Rows persist and load |
| 4 | DTOs and mapping | Entity is never exposed directly |
| 5 | Service layer | CRUD, search/filter, duplicate check |
| 6 | Controller | All 5 required endpoints return the correct status codes |
| 7 | Error handling | Every failure returns the same JSON error shape |
| 8 | Seed data | Empty DB gets sample rows once, never overwrites |
| 9 | Tests | Service, controller and repository tests pass |
| 10 | Manual check with curl | Every endpoint and error case verified against the running app |
| 11 | Backend README section and API docs | Another person can run it from the README |

---

## 2. Docker Compose (MySQL)

`docker-compose.yml` at the repo root. It runs the database only for now. The backend and frontend services are added later as a bonus.

```yaml
services:
  mysql:
    image: mysql:latest
    container_name: policy-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: policy_db
    ports:
      - "3306:3306"          # app default port is 3306 (see section 3)
    volumes:
      - mysql_data:/var/lib/mysql
volumes:
  mysql_data:
```

- Data persists in the named volume. `docker compose down -v` wipes it.
- Credentials are dev-only defaults. The README will say so, and they can be overridden by environment variables.

---

## 3. Dependencies and configuration

### `pom.xml` changes
- Add `spring-boot-starter-validation`.
- Add `com.h2database:h2` in test scope (tests only, MySQL is used at runtime).
- Fill in the empty `<name>`, `<description>`, `<licenses>`, `<developers>`, `<scm>` blocks. The empty tags are generator leftovers.
- Optional: `springdoc-openapi` for Swagger UI. It is added only if a Boot 4 compatible version resolves cleanly. Otherwise the API is documented in the README.
- Keep Lombok. The entity uses `@Getter/@Setter`, not `@Data`, to avoid broken `equals/hashCode` on JPA entities.

### `application.properties`
Keep the existing env-var-driven style and extend it:
```properties
spring.datasource.url=jdbc:mysql://${DATABASE_HOST:localhost}:${DATABASE_PORT:3306}/${DATABASE_NAME:policy_db}
# schema is owned by the SQL scripts, not by Hibernate
spring.jpa.hibernate.ddl-auto=none
spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:sql/policy-schema.sql
spring.sql.init.data-locations=classpath:sql/policy-data.sql
spring.jpa.open-in-view=false
spring.jackson.default-property-inclusion=non_null
app.cors.allowed-origins=${CORS_ORIGINS:http://localhost:5173}
app.pagination.default-limit=10
app.pagination.max-limit=100
```
- The database name changes from `jobportal` to `policy_db`.
- The default port changes from 3308 to **3306** to match the current `docker-compose.yml` (`3306:3306`). The compose file's comment still says 3308, which I'll correct.
- `ddl-auto=none`: the SQL scripts in `src/main/resources/sql/` are the source of truth for the schema (see section 4a).
- The `show_sql` default stays, but I'd flip it to false for cleaner logs, with the env var to turn it on.
- `src/test/resources/application.properties` points tests at in-memory H2.

---

## 4. Domain model

### `Policy` entity (table `policies`)
| Field | Type / column | Rules |
|---|---|---|
| id | `Long`, identity | Never taken from client input |
| policyNumber | `varchar(50)` | not null, **unique constraint** |
| customerName | `varchar(120)` | not null |
| policyType | enum as STRING | not null |
| premiumAmount | `decimal(12,2)` | not null, > 0 |
| status | enum as STRING | not null |
| startDate | `date` | not null |
| createdAt | `datetime` | set once on insert, not updatable |
| updatedAt | `datetime` | set on every update (small addition, useful for audit) |

Indexes: unique on `policy_number`, plus indexes for the status filter and the sortable columns.

### 4a. SQL scripts (`src/main/resources/sql/`)
| File | Purpose |
|---|---|
| `policy-schema.sql` (written) | `CREATE TABLE IF NOT EXISTS policies`, with a primary key, a unique key on `policy_number`, `CHECK` constraints (type list, status list, premium > 0), indexes `(status, created_at)`, `customer_name`, `start_date`, utf8mb4 and InnoDB |
| `policy-data.sql` (written) | 12 sample policies via `INSERT IGNORE`. It is idempotent thanks to the unique key. This **replaces the `DataSeeder` class** |

- Both are run by Spring at startup (`spring.sql.init.*`), so a fresh MySQL container needs no manual steps.
- The DB enforces the same rules as the API (defence in depth), so bad data can't get in even through a direct SQL insert.
- Enum values are stored as upper-case strings (`ACTIVE`, `LIFE`). The entity's `@Column` names match (`policy_number`, `customer_name`, ...).
- Tests use H2 with `ddl-auto=create-drop` and skip these scripts, because MySQL-specific syntax such as `ON UPDATE` is not portable.

### Enums
- `PolicyType`: `LIFE, HEALTH, MOTOR, TRAVEL`
- `PolicyStatus`: `ACTIVE, PENDING, CANCELLED`
- In JSON they appear as `"Active"`, `"Life"` (matching the assessment wording). They are accepted case-insensitively and stored as upper-case strings in the DB. An unknown value gives a 400 that lists the allowed values.

---

## 5. API design

Base path `/api/policies`, JSON only.

### How `WebConfig` shapes the API (existing file, `config/web/WebConfig.java`)
- `addPathPrefix("/api", _ -> true)` puts `/api` on **every** controller. So `PolicyController` is mapped as `@RequestMapping("/policies")`, **not** `/api/policies`. Writing `/api/policies` would produce `/api/api/policies`.
- Media-type API versioning is already on: `Accept: application/vnd.xerago+json;v=1.0`, supported versions 1.0 to 3.0, default **1.0**. A plain request with no version or with `application/json` is served as v1.0, so curl and the React app need nothing special.
- I'll leave the version config alone. Mappings stay unversioned (they serve v1.0). If you want explicit `version = "1.0"` on the controller methods, that's a one-line change.
- CORS will be added to this same class (`addCorsMappings`), not a separate `CorsConfig`.

### Endpoints
| Method | Path | Purpose | Success | Failure |
|---|---|---|---|---|
| GET | `/api/policies` | Paginated, sorted, filtered list | 200 | 400 bad param |
| GET | `/api/policies/{id}` | One policy | 200 | 404 |
| POST | `/api/policies` | Create | **201** + `Location: /api/policies/{id}` | 400, 409 |
| PUT | `/api/policies/{id}` | Full update | 200 | 400, 404, 409 |
| DELETE | `/api/policies/{id}` | Delete | **204** no body | 404 |

### Design decisions
1. **Filtering:** `search` (matches customer name **or** policy number, case-insensitive, trimmed, `%` and `_` escaped so they are literal) and `status` (case-insensitive). Both optional and combinable. A blank `search` is ignored.
2. **Pagination, sorting and limit** (always on, with safe defaults):

   | Param | Default | Rules |
   |---|---|---|
   | `page` | `0` | 0-based (matches Spring), must be >= 0 |
   | `limit` | `10` | 1 to 100. Over 100 gives a **400**, not a silent cap, so the client knows |
   | `sort` | `createdAt,desc` | `field,asc\|desc`. Whitelist: `policyNumber, customerName, policyType, premiumAmount, status, startDate, createdAt`. Direction is optional (defaults to asc). Can be repeated for multi-column sort. An unknown field gives a 400 listing the allowed fields |
   | `search`, `status` | none | see above |

   - A stable tie-breaker `id` is always appended to the sort so pages never overlap or skip rows.
   - **The response is a page envelope that includes the limit:**
     ```json
     {
       "content": [ { "id": 1, "policyNumber": "POL-1001", "...": "..." } ],
       "page": 0,
       "limit": 10,
       "totalElements": 42,
       "totalPages": 5,
       "first": true,
       "last": false,
       "sort": ["createdAt,desc"]
     }
     ```
   - An empty result is `200` with `content: []` and `totalElements: 0`, not a 404. The UI uses this for the empty state.
   - A `page` past the last one returns an empty `content` with the real totals, so the client can detect it and step back.
   - **Trade-off:** the assessment says GET "returns all policies", and an envelope is a different shape from a bare array. It's the better design for a UI with paging, but it's worth stating in the README and walkthrough.
   - The envelope is a small custom `PageResponse<T>` record, not a serialized Spring `Page` (its JSON shape is unstable and Spring warns against it).
3. **PUT is a full replacement.** All fields are required, and `id` comes only from the path. Any `id` in the body is ignored. PATCH is out of scope.
4. **Policy number rules:** trimmed, and stored upper-case. Uniqueness is case-insensitive in effect, so `pol-1` and `POL-1` conflict. On update, the same policy keeping its own number is **not** a conflict.
5. **Premium** is serialized as a number with 2 decimals. `BigDecimal` is used end to end, never `double`.
6. **Dates** use ISO format `yyyy-MM-dd`. Timestamps are ISO-8601.
7. **No stack traces or SQL in responses.** They are logged server-side only.
8. **Idempotency:** DELETE on an already-deleted ID returns 404, as the assessment allows.

### Request body (`POST` and `PUT`)
```json
{
  "policyNumber": "POL-1001",
  "customerName": "Asha Raman",
  "policyType": "Health",
  "premiumAmount": 12500.00,
  "status": "Active",
  "startDate": "2026-11-01"
}
```

### Response body
```json
{
  "id": 1,
  "policyNumber": "POL-1001",
  "customerName": "Asha Raman",
  "policyType": "Health",
  "premiumAmount": 12500.00,
  "status": "Active",
  "startDate": "2026-11-01",
  "createdAt": "2026-10-29T10:15:30"
}
```

### Validation rules (`PolicyRequest`)
| Field | Rule | Message |
|---|---|---|
| policyNumber | `@NotBlank`, max 50, pattern `[A-Za-z0-9-_/]+` | "Policy number is required" |
| customerName | `@NotBlank`, max 120 | "Customer name is required" |
| policyType | `@NotNull` | "Policy type is required" |
| premiumAmount | `@NotNull`, `@DecimalMin(0, exclusive)`, `@Digits(10,2)` | "Premium must be greater than zero" |
| status | `@NotNull` | "Status must be Active, Pending or Cancelled" |
| startDate | `@NotNull`, valid ISO date | "Start date must be a valid date (yyyy-MM-dd)" |

Start date is **not** restricted to the future or past. A policy may legitimately start earlier or later. I'll note that as an assumption.

### Error response (one shape for all errors)
```json
{
  "timestamp": "2026-10-29T10:15:30",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/policies",
  "fieldErrors": [
    { "field": "premiumAmount", "message": "Premium must be greater than zero" }
  ]
}
```
`fieldErrors` is only present for validation failures. This is a custom record. I'll also check whether Spring's built-in `ProblemDetail` (RFC 9457) is the better fit and pick one. Either way the shape stays consistent.

| Exception | Status |
|---|---|
| `MethodArgumentNotValidException` | 400 with field errors |
| `HttpMessageNotReadableException` (bad JSON, bad enum, bad date) | 400 with a readable message and the allowed values |
| `MethodArgumentTypeMismatchException` (e.g. `/api/policies/abc`, `?page=x`) | 400 |
| `HandlerMethodValidationException` / `ConstraintViolationException` (`page < 0`, `limit > 100`) | 400 with field errors |
| `InvalidSortException` (sort field not whitelisted) | 400 listing the allowed fields |
| `PolicyNotFoundException` | 404 |
| `DuplicatePolicyException`, `DataIntegrityViolationException` | 409 (the DB constraint backs up the service check in race conditions) |
| `HttpRequestMethodNotSupportedException`, unsupported media type | 405 / 415 |
| anything else | 500, generic message, logged |

---

## 6. Layers

```
com.xerago.backend
├── BackendApplication
├── config/web/    WebConfig (exists: API versioning, /api prefix; CORS added here)
├── controller/    PolicyController (@RequestMapping("/policies"); no class-level @Validated, so param constraints raise HandlerMethodValidationException)
├── service/       PolicyService, PolicyServiceImpl
├── repository/    PolicyRepository (JpaRepository + JpaSpecificationExecutor)
├── entity/        Policy, PolicyType, PolicyStatus
├── dto/           PolicyRequest, PolicyResponse, PageResponse<T>, ErrorResponseDto (exists)
├── mapper/        PolicyMapper (small manual mapper, no MapStruct needed)
├── specification/ PolicySpecifications (search + status predicates)
└── exception/     PolicyNotFoundException, DuplicatePolicyException, GlobalExceptionHandler
```

- **Controller:** HTTP only. `@Valid` on request bodies, status codes, `Location` header. No logic.
- **Service:** business rules, `@Transactional` (read-only for queries), duplicate check, and entity-to-DTO mapping through the mapper.
- **Repository:** Spring Data. `existsByPolicyNumberIgnoreCase` and `existsByPolicyNumberIgnoreCaseAndIdNot`, plus Specifications for the search.
- Constructor injection throughout (Lombok `@RequiredArgsConstructor`).

---

## 7. Seed data
Seed data lives in `sql/policy-data.sql` (12 varied policies covering every type and status, enough for 2 pages at the default limit). `INSERT IGNORE` plus the unique key means it never duplicates or overwrites. It is initial data only, and the app reads everything from MySQL. No Java seeder class is needed.

---

## 8. Testing plan
| Layer | Tool | Cases |
|---|---|---|
| Service | JUnit 5 + Mockito | create ok, duplicate number, update not found, update keeps own number, delete not found, search passes filters |
| Controller | `@WebMvcTest` + MockMvc | 201 + Location, 400 field errors, 400 bad enum, 400 bad date, 404, 409, 204, list filters, `limit=0` / `limit=101` / `page=-1` give 400, bad `sort` field gives 400, default version works with no Accept header |
| Repository | `@DataJpaTest` + H2 | unique constraint, search by name or number (case-insensitive, `%` literal), status filter, combined filter, paging totals, sort order and tie-breaker |
| Smoke | `@SpringBootTest` | context loads (already exists) |

Spring Boot 4 moved the test slice annotations into new packages, so I'll verify the imports at compile time.

---

## 9. Verification before calling the backend done
1. `docker compose up -d`, wait for healthy.
2. `./mvnw clean verify`: all tests green.
3. `./mvnw spring-boot:run` starts on port 8000.
4. curl, or a `requests.http` file, covering: list, filter, search, get, create, validation errors, duplicate (409), update, update missing (404), delete, delete missing (404).
5. Restart the app and confirm the data is still there.

---

## 10. Assumptions to document in the README
- MySQL is run through Docker Compose on host port 3306, with dev-only credentials.
- The schema is created by `sql/policy-schema.sql` on startup. Production would use Flyway or Liquibase migrations (the trade-off mentioned in the walkthrough).
- The list endpoint returns a page envelope (with `limit`), not a bare array.
- Policy numbers are normalized to upper-case and unique case-insensitively.
- There are no restrictions on start date range.
- No authentication (JWT is out of scope).

---

## 11. Decisions for you

1. **Rename the DB** from `jobportal` to `policy_db` (recommended).
2. **Pagination and sorting:** now decided as always-on with a page envelope that includes `limit` (see section 5). Say so if you'd rather keep a bare array.
3. **Swagger UI via springdoc:** include if it works with Boot 4.1 (recommended), or skip?
4. **Error format:** custom `ErrorResponse` (recommended, simple to explain) or Spring `ProblemDetail`?
5. **`requests.http` file** for manual testing, kept in the repo (recommended)?
