# Policy Management Portal

A full-stack web application for managing insurance policies: list, search, filter, sort, paginate, create, edit and delete.

- **Frontend:** React 19 + Vite (JavaScript), React Router
- **Backend:** Spring Boot 4.1 (Java 25), Spring Data JPA, Bean Validation
- **Database:** MySQL (Docker)

---

## Quick start (Docker)

One command starts MySQL, the backend and the frontend. The only prerequisite is [Docker](https://www.docker.com/products/docker-desktop/). Open a terminal in the **project root** (the folder that contains `docker-compose.yml`) and run:

**Windows PowerShell**

```powershell
$env:FRONTEND_PORT="3100"; $env:BACKEND_PORT="8100"; $env:MYSQL_PORT="3307"; docker compose up --build
```

**macOS / Linux / Git Bash**

```bash
FRONTEND_PORT=3100 BACKEND_PORT=8100 MYSQL_PORT=3307 docker compose up --build
```

The first build takes a few minutes because it compiles the backend and the frontend inside Docker. The backend needs about 40 seconds more to start, so refresh if the page briefly shows `502 Bad Gateway`. Then open:

| Service | URL |
|---|---|
| Application (UI) | http://localhost:3100 |
| REST API | http://localhost:8100/api/policies |
| MySQL | `localhost:3307` (user `root`, password `root`, database `policy_db`) |

The database starts empty. Click **New policy** in the UI to add your first policy.

**Prefer the default ports?** Run plain `docker compose up --build`. The UI is then at http://localhost:3000, the API at http://localhost:8000/api/policies and MySQL on `localhost:3306`. Use the custom ports above if any of those ports is already taken on your machine. If one is, Docker starts MySQL but not the container that needs the busy port (see [Troubleshooting](#troubleshooting)). You can change just one of the three variables.

### Stop the application

```bash
docker compose down        # keep the data
docker compose down -v     # also delete the database volume
```

In PowerShell the port variables stay set for the rest of that session. To clear them:

```powershell
Remove-Item Env:FRONTEND_PORT, Env:BACKEND_PORT, Env:MYSQL_PORT
```

---

## Table of contents

1. [Quick start (Docker)](#quick-start-docker)
2. [Features](#features)
3. [Run locally for development](#run-locally-for-development)
4. [Prerequisites](#prerequisites)
5. [Database setup](#database-setup)
6. [API summary](#api-summary)
7. [Validation rules](#validation-rules)
8. [Error responses](#error-responses)
9. [Tests](#tests)
10. [Troubleshooting](#troubleshooting)
11. [Project structure](#project-structure)
12. [Architecture and design decisions](#architecture-and-design-decisions)
13. [Assumptions](#assumptions)

---

## Features

- **Policy list** with Policy Number, Customer, Type, Premium, Status and Start Date. It is a table on desktop and turns into cards on phones.
- **Create and edit** through one form with client-side validation and server-side error messages shown next to the fields.
- **Delete** with a confirmation dialog.
- **Search** by customer name or policy number, **filter** by status and policy type, and a **Reset** button.
- **Sorting** by clicking any column header, and **pagination** with a rows-per-page selector.
- **Feedback:** loading skeletons, empty states, error banners with Retry, and toast notifications such as "Policy created successfully".
- **Light and dark theme** with a toggle. The choice is remembered, and the first visit follows the OS setting.
- **Navigation:** Customers and Reports are placeholders that show an "under construction" page.
- Data is stored in MySQL through Spring Data JPA, so it survives a page refresh and a restart.

---

## Run locally for development

Use this when you want to change code. You need the [prerequisites](#prerequisites) below.

### 1. Backend

```bash
cd backend
./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run
```

The backend starts on **http://localhost:8000**. It uses Spring Boot's Docker Compose support, so it starts the MySQL container from `backend/docker-compose.yml` by itself and stops it again on exit. You do not need to start the database manually.

If you prefer your own MySQL server, set these environment variables and disable the automatic Docker startup:

| Variable | Default |
|---|---|
| `DATABASE_HOST` | `localhost` |
| `DATABASE_PORT` | `3306` |
| `DATABASE_NAME` | `policy_db` |
| `DATABASE_USERNAME` | `root` |
| `DATABASE_PASSWORD` | `root` |
| `SERVER_PORT` | `8000` |

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--spring.docker.compose.enabled=false
```

### 2. Frontend

```bash
cd frontend
npm install
npm run dev
```

Open **http://localhost:5173**. The dev server forwards every `/api` request to the backend on port 8000. To point it at a different backend, set `VITE_PROXY_TARGET`, for example `VITE_PROXY_TARGET=http://localhost:8100 npm run dev`.

To create a production build: `npm run build` (output in `frontend/dist`).

---

## Prerequisites

| For | You need |
|---|---|
| Docker quick start | Docker with Docker Compose v2 |
| Backend (local) | JDK 25 and Docker (for the database). The Maven wrapper is included, so Maven is not required. |
| Frontend (local) | Node.js 22 or newer and npm |

---

## Database setup

No manual setup is needed. On startup the backend runs `backend/src/main/resources/sql/policy-schema.sql`, which creates the `policies` table if it does not exist: primary key, unique `policy_number`, check constraints for type, status, premium and policy number format, and indexes for filtering and sorting. It is safe to run on every start.

The application does not load any sample data, so a new database starts **empty**. Create policies from the UI or through the API.

The database itself (`policy_db`) is created by the MySQL container from the `MYSQL_DATABASE` setting. Hibernate does not manage the schema (`ddl-auto=none`); the SQL scripts are the source of truth.

The credentials (`root` / `root`) are development defaults. Change them in the compose files and in the `DATABASE_*` variables before using this anywhere real.

To inspect the data, connect any MySQL client to `localhost:3306`.

---

## API summary

Base URL: `http://localhost:8000/api`. All requests and responses use JSON.

| Method | Endpoint | Description | Success | Errors |
|---|---|---|---|---|
| GET | `/policies` | List policies (paginated, sorted, filtered) | 200 | 400 |
| GET | `/policies/{id}` | Get one policy | 200 | 404 |
| POST | `/policies` | Create a policy | **201** with a `Location` header | 400, 409 |
| PUT | `/policies/{id}` | Replace a policy's details | 200 | 400, 404, 409 |
| DELETE | `/policies/{id}` | Delete a policy | **204** (no body) | 404 |

### List query parameters (`GET /policies`)

| Parameter | Default | Description |
|---|---|---|
| `search` | none | Matches part of the customer name **or** the policy number, case-insensitive. |
| `status` | none | `Active`, `Pending` or `Cancelled` (case-insensitive). |
| `policyType` | none | `Life`, `Health`, `Motor` or `Travel` (case-insensitive). |
| `page` | `0` | Page number, starting at 0. |
| `limit` | `10` | Page size, 1 to 50. A larger value returns 400. |
| `sort` | `createdAt` | One or more of `policyNumber`, `customerName`, `policyType`, `premiumAmount`, `status`, `startDate`, `createdAt`. |
| `direction` | `desc` for the default sort, otherwise `asc` | `asc` or `desc`. |

`sort=premiumAmount,desc` also works. The filters can be combined, for example:

```
GET /api/policies?search=asha&status=active&policyType=health&sort=startDate&direction=desc&page=0&limit=10
```

### Request body (POST and PUT)

```json
{
  "policyNumber": "1001",
  "customerName": "Asha Raman",
  "policyType": "Health",
  "premiumAmount": 12500.00,
  "status": "Active",
  "startDate": "2026-01-15"
}
```

- `policyNumber` is **digits only** (1 to 10). The server stores and returns it as `POL-` plus the digits, so `1001` becomes `POL-1001`. When editing, send the digits without the prefix.
- `policyType` and `status` are case-insensitive. `startDate` uses `yyyy-MM-dd`.
- PUT replaces all fields. The `id` always comes from the URL; any `id` in the body is ignored.

### Policy response

```json
{
  "id": 1,
  "policyNumber": "POL-1001",
  "customerName": "Asha Raman",
  "policyType": "Health",
  "premiumAmount": 12500.00,
  "status": "Active",
  "startDate": "2026-01-15",
  "createdAt": "2026-10-29T11:56:37.362664"
}
```

### List response

The list endpoint returns a page envelope, not a bare array:

```json
{
  "content": [ { "id": 1, "policyNumber": "POL-1001", "...": "..." } ],
  "page": 0,
  "limit": 10,
  "totalElements": 12,
  "totalPages": 2,
  "first": true,
  "last": false,
  "sort": ["createdAt,desc"]
}
```

An empty result is a normal `200` with an empty `content` list.

### Other notes

- **CORS:** all origins are allowed on `/api/**`.
- **API versioning:** the default version is `1.0`, so plain requests work. A client may also send `Accept: application/vnd.xerago+json;v=1.0`.

---

## Validation rules

Rules are enforced in three places: the React form (fast feedback), the Spring `@Valid` request DTO (the real check), and the database constraints (a final safety net).

| Field | Rule |
|---|---|
| `policyNumber` | Required, digits only, at most 10 digits. Must be unique (409 if it already exists). |
| `customerName` | Required, at most 120 characters. Surrounding spaces are trimmed. |
| `policyType` | Required: Life, Health, Motor or Travel. |
| `premiumAmount` | Required, greater than zero, at most 10 digits and 2 decimals. |
| `status` | Required: Active, Pending or Cancelled. |
| `startDate` | Required, a valid date in `yyyy-MM-dd` format. |

All field errors are reported together, not one at a time.

---

## Error responses

Every error uses the same JSON shape, including unknown routes and unsupported methods:

```json
{
  "timestamp": "2026-10-29T15:08:35.388",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/policies",
  "fieldErrors": [
    { "field": "policyType", "message": "Invalid value 'car'. Allowed values: Life, Health, Motor, Travel." },
    { "field": "premiumAmount", "message": "Premium amount must be greater than zero" }
  ]
}
```

`fieldErrors` only appears for validation problems.

| Status | When |
|---|---|
| 400 | Validation failed, malformed JSON, an invalid parameter such as `limit=100` or an unknown sort field. |
| 404 | The policy does not exist, or the route does not exist. |
| 405 | The HTTP method is not supported for the route. |
| 409 | The policy number already exists. |
| 500 | Unexpected error. The response contains a generic message, and the details are only logged. |

---

## Tests

Backend tests (38) run against an in-memory H2 database, so no MySQL is needed:

```bash
cd backend
./mvnw test
```

- **Service unit tests** (Mockito): create, update, delete, duplicate detection, sorting and direction parsing.
- **API integration tests** (MockMvc, full application context): every endpoint, status codes, pagination, sorting, search, wildcard characters in search, all filters, validation messages, error format and CORS.

The frontend has no automated tests.

---

## Troubleshooting

**Only MySQL starts, the backend and frontend stay stopped.** A host port is already in use. The error looks like `ports are not available: exposing port TCP 0.0.0.0:8000 ... Only one usage of each socket address`. Port 8000 is often held by the backend running from an IDE or a terminal. Either stop that process, or start the stack on other ports (the [Quick start](#quick-start-docker) commands already do this). To find what is using a port on Windows:

```powershell
netstat -ano | findstr :8000
tasklist /FI "PID eq <pid-from-the-last-column>"
```

**The page shows `502 Bad Gateway` right after start.** The backend takes about 40 seconds to start. Wait a moment and refresh.

**Containers from an earlier run are in the way.** Remove them and start again:

```bash
docker compose down
docker compose up --build
```

Add `-v` to `docker compose down` to also delete the database volume and start with a fresh database.

**I want to start again with an empty database.** Run `docker compose down -v` (this deletes all stored policies) and start the stack again.

**`Unknown database 'policy_db'` when running the backend locally.** The MySQL volume was created before the database existed, usually because the first start was interrupted. Remove the volume (`docker compose down -v` in the `backend` folder) and start again.

---

## Project structure

```
policy_management_portal/
├── docker-compose.yml          full stack: MySQL + backend + frontend
├── README.md
├── backend/
│   ├── Dockerfile
│   ├── docker-compose.yml      MySQL only, used by `spring-boot:run`
│   └── src/main/
│       ├── java/com/xerago/backend/
│       │   ├── config/web/     WebConfig: /api prefix, versioning, CORS, enum converters
│       │   ├── controller/     PolicyController
│       │   ├── service/        PolicyService + impl/PolicyServiceImpl
│       │   ├── repository/     PolicyRepository (JPA + Specifications)
│       │   ├── specification/  search and filter predicates
│       │   ├── entity/         Policy, PolicyType, PolicyStatus
│       │   ├── dto/            request, response, page and error objects
│       │   ├── mapper/         PolicyMapper
│       │   ├── validation/     @ValidEnum, @ValidIsoDate
│       │   └── exception/      domain exceptions + GlobalExceptionHandler
│       └── resources/
│           ├── application.properties
│           └── sql/            policy-schema.sql
└── frontend/
    ├── Dockerfile, nginx.conf
    └── src/
        ├── api/                policyApi.js (all backend calls)
        ├── components/
        │   ├── common/         Button, Modal, ConfirmDialog, Pagination, form controls, icons...
        │   ├── layout/         AppHeader, UserMenu
        │   └── policy/         PolicyTable, PolicyFilters, PolicyFormModal, StatusBadge
        ├── constants/          policy options, navigation
        ├── hooks/              usePolicies, useToasts, useTheme, usePageTitle
        ├── pages/              PoliciesPage, UnderConstructionPage, NotFoundPage
        ├── styles/             tokens.css (all colours), base, common, layout, policy
        └── utils/              formatting and form validation
```

---

## Architecture and design decisions

**Backend layering.** `controller → service → repository`. Controllers only handle HTTP and return `ResponseEntity`. The service holds the rules (duplicate checks, sorting whitelist, transactions). The repository is Spring Data JPA. DTOs keep the API shape separate from the entity, so a client can never set `id` or `createdAt`.

**Validation.** Bean Validation on the request DTO collects every error at once. Enum and date fields are received as strings and checked by small custom validators; this is what lets the API report an invalid type, status and date together instead of stopping at the first one. A unique constraint in the database backs up the duplicate check when two requests arrive at the same moment.

**Consistent errors.** One `@RestControllerAdvice` extends Spring's `ResponseEntityExceptionHandler`, so framework errors (405, unknown routes) come back in the same format as application errors.

**Server-side search, sort and pagination.** The backend builds a dynamic JPA `Specification` from the filters and applies a whitelist to the sort fields, so a bad sort field is a clear 400 instead of a database error. The UI waits 350 ms after typing before searching and cancels outdated requests.

**Frontend structure.** Presentational building blocks live in `components/common` and know nothing about policies. Policy-specific components live in `components/policy`. All data access is in one file (`api/policyApi.js`), and state for the list lives in the `usePolicies` hook. Every colour is defined once in `styles/tokens.css`, which also provides the dark theme.

**Docker.** Both services build inside Docker (multi-stage), so the host needs nothing but Docker. nginx serves the React build and forwards `/api` to the backend, so the browser talks to a single origin.

### Design trade-offs

1. **Page envelope instead of a bare array.** The assessment says the list endpoint returns "all policies". Returning an array would be simpler, but it cannot carry the page and total counts a paginated UI needs. The envelope also tells the client the `limit` and the applied sort. The cost is a slightly different shape from the minimum in the brief.
2. **SQL scripts instead of `ddl-auto=update`.** The schema and its constraints (such as the `POL-` format check) are explicit and reviewable. The cost is that schema changes are edited by hand. A real project would use Flyway or Liquibase migrations instead.
3. **Server-side filtering instead of filtering in the browser.** It keeps the response small and works with pagination, at the cost of one request per change. Debouncing and request cancellation reduce that cost.

---

## Assumptions

- Policy numbers are entered as digits only and stored as `POL-<digits>`. They are unique.
- Premium amounts are shown in Indian rupees (₹). The API does not store a currency.
- A start date may be in the past or the future; there is no range restriction.
- There is no authentication. The user name and menu in the header are placeholders, and JWT login was left out.
- Customers and Reports are not built. They show an under-construction page.
- The database credentials in the compose files are for local development only.
- The database starts empty; no sample data is loaded.
