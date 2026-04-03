# Finance Data Processing & Access Control Backend

A production-style Spring Boot REST API for a finance dashboard system with role-based access control, financial record management, and analytics.

---

## Tech Stack

| Component | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2.5 |
| Security | Spring Security + JWT (jjwt 0.11.5) |
| Database | H2 In-Memory (auto-configured, zero setup) |
| ORM | Spring Data JPA / Hibernate |
| Validation | Jakarta Bean Validation |
| Docs | SpringDoc OpenAPI 2.x (Swagger UI) |
| Build | Maven |
| Tests | JUnit 5 + Mockito |

---

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+

### Run the application

```bash
cd finance-backend
mvn spring-boot:run
```

The server starts at **http://localhost:8080**

---

## Seeded Test Accounts

Three users are automatically created on startup:

| Role | Email | Password |
|---|---|---|
| **ADMIN** | admin@finance.com | Admin@123 |
| **ANALYST** | analyst@finance.com | Analyst@123 |
| **VIEWER** | viewer@finance.com | Viewer@123 |

12 sample financial records are also seeded for testing the dashboard.

---

## API Documentation

Swagger UI: **http://localhost:8080/swagger-ui.html**
OpenAPI JSON: **http://localhost:8080/api-docs**
H2 Console: **http://localhost:8080/h2-console** (JDBC URL: `jdbc:h2:mem:financedb`)

### How to use Swagger UI
1. Open http://localhost:8080/swagger-ui.html
2. Use `POST /api/auth/login` with your credentials
3. Copy the `token` from the response
4. Click **Authorize** at the top → paste the token
5. All authenticated endpoints are now accessible

---

## API Reference

### Authentication (`/api/auth`)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Self-register (gets VIEWER role) |
| POST | `/api/auth/login` | Public | Login, returns JWT |
| GET | `/api/auth/me` | Authenticated | Current user profile |

**Login request:**
```json
{
  "email": "admin@finance.com",
  "password": "Admin@123"
}
```

**Login response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "email": "admin@finance.com",
  "name": "System Admin",
  "role": "ADMIN"
}
```

---

### User Management (`/api/users`) — ADMIN only

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/users` | List all users (paginated) |
| GET | `/api/users/{id}` | Get user by ID |
| POST | `/api/users` | Create user with explicit role |
| PUT | `/api/users/{id}` | Update name, role, or active status |
| DELETE | `/api/users/{id}` | Deactivate user |

**Create user request:**
```json
{
  "name": "New Analyst",
  "email": "newanalyst@finance.com",
  "password": "SecurePass@1",
  "role": "ANALYST"
}
```

---

### Financial Records (`/api/records`)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/records` | ADMIN | Create a record |
| GET | `/api/records` | ANALYST, ADMIN | List with filters + pagination |
| GET | `/api/records/{id}` | ANALYST, ADMIN | Get single record |
| PUT | `/api/records/{id}` | ADMIN | Update a record |
| DELETE | `/api/records/{id}` | ADMIN | Soft delete (recoverable) |

**Create record request:**
```json
{
  "amount": 85000.00,
  "type": "INCOME",
  "category": "Salary",
  "date": "2024-04-01",
  "notes": "Monthly salary"
}
```

**Filter parameters for GET `/api/records`:**
- `type` → `INCOME` or `EXPENSE`
- `category` → e.g., `Salary`
- `startDate` → `2024-01-01` (ISO format)
- `endDate` → `2024-12-31`
- `page`, `size`, `sort` → pagination (default: 20 per page, sorted by date desc)

---

### Dashboard Analytics (`/api/dashboard`)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/api/dashboard/summary` | ALL | Full summary (totals + categories + trends + recent) |
| GET | `/api/dashboard/by-category` | ALL | Category-wise totals |
| GET | `/api/dashboard/recent?count=10` | ALL | Recent N transactions |
| GET | `/api/dashboard/trends?months=12` | ANALYST, ADMIN | Monthly income/expense trends |

**Summary response example:**
```json
{
  "totalIncome": 200000.00,
  "totalExpenses": 65000.00,
  "netBalance": 135000.00,
  "totalRecords": 12,
  "categoryBreakdown": {
    "Salary": 170000.00,
    "Rent": 50000.00,
    "Groceries": 16300.00
  },
  "monthlyTrends": [
    { "month": "2024-04", "income": 102000.00, "expenses": 40300.00 },
    { "month": "2024-03", "income": 100000.00, "expenses": 25000.00 }
  ],
  "recentActivity": [...]
}
```

---

## Access Control Matrix

| Feature | VIEWER | ANALYST | ADMIN |
|---|---|---|---|
| Dashboard summary | ✅ | ✅ | ✅ |
| Category breakdown | ✅ | ✅ | ✅ |
| Recent activity | ✅ | ✅ | ✅ |
| Monthly trends | ❌ | ✅ | ✅ |
| List financial records | ❌ | ✅ | ✅ |
| View single record | ❌ | ✅ | ✅ |
| Create record | ❌ | ❌ | ✅ |
| Update record | ❌ | ❌ | ✅ |
| Delete record | ❌ | ❌ | ✅ |
| User management | ❌ | ❌ | ✅ |

---

## Validation & Error Handling

All endpoints return structured error responses:

```json
{
  "status": 400,
  "error": "Validation Failed",
  "message": "One or more fields have errors",
  "path": "/api/records",
  "timestamp": "2024-04-03T10:00:00",
  "fieldErrors": {
    "amount": "Amount must be greater than zero",
    "date": "Date cannot be in the future"
  }
}
```

**HTTP status codes used:**
- `200 OK` — successful read
- `201 Created` — successful creation
- `204 No Content` — successful deletion
- `400 Bad Request` — validation failure or invalid input
- `401 Unauthorized` — missing or invalid JWT
- `403 Forbidden` — insufficient role
- `404 Not Found` — resource does not exist
- `409 Conflict` — duplicate email on registration
- `500 Internal Server Error` — unexpected server error

---

## Project Structure

```
src/main/java/com/finance/
├── FinanceApplication.java         # Entry point
├── config/
│   ├── SecurityConfig.java         # Spring Security + JWT config
│   ├── OpenApiConfig.java          # Swagger UI config
│   └── DataInitializer.java        # Seed data on startup
├── auth/
│   ├── AuthController.java         # /api/auth endpoints
│   ├── AuthService.java            # Register/login logic
│   ├── CustomUserDetailsService.java
│   ├── JwtFilter.java              # JWT extraction filter
│   └── JwtUtil.java                # Token generation/validation
├── user/
│   ├── User.java                   # JPA entity
│   ├── Role.java                   # VIEWER, ANALYST, ADMIN enum
│   ├── UserRepository.java
│   ├── UserService.java            # CRUD for users
│   └── UserController.java         # /api/users (ADMIN only)
├── finance/
│   ├── FinancialRecord.java        # JPA entity with soft delete
│   ├── RecordType.java             # INCOME, EXPENSE enum
│   ├── FinancialRecordRepository.java  # Aggregation queries
│   ├── FinancialRecordService.java
│   └── FinancialRecordController.java  # /api/records
├── dashboard/
│   ├── DashboardService.java       # Aggregation logic
│   └── DashboardController.java    # /api/dashboard
├── dto/
│   ├── request/                    # Validated input DTOs
│   └── response/                   # Clean output DTOs
└── exception/
    ├── GlobalExceptionHandler.java # @ControllerAdvice
    ├── ErrorResponse.java          # Standard error format
    ├── ResourceNotFoundException.java
    └── DuplicateResourceException.java
```

---

## Assumptions & Design Decisions

1. **H2 in-memory database** — chosen for zero-setup evaluation. Data resets on restart. To switch to PostgreSQL, update `application.yml` datasource URL and add the PG driver to `pom.xml`.

2. **Self-registered users get VIEWER role** — only ADMIN can assign higher roles.

3. **Soft deletes for financial records** — records are never hard-deleted; a `deleted=true` flag + `deletedAt` timestamp is used. This maintains audit history.

4. **BigDecimal for amounts** — avoids floating-point precision issues with monetary values.

5. **JWT expiry** — set to 24 hours. Can be tuned in `application.yml`.

6. **Roles are fixed** — `VIEWER`, `ANALYST`, `ADMIN` are defined in code (not a DB table) for simplicity and type safety.

7. **No password reset flow** — out of scope; admin can create a new user.

---

## Running Tests

```bash
mvn test
```

Tests cover:
- `AuthServiceTest` — registration, duplicate email, deactivated user login, bad credentials
- `FinancialRecordServiceTest` — create, not found, soft delete behavior

---

## Potential Enhancements (Not Implemented)

- PostgreSQL / MySQL for persistent storage
- Password reset flow
- Refresh token endpoint
- Rate limiting (e.g., Bucket4j)
- Audit log table for all state changes
- Docker Compose file for containerized deployment
- Soft-delete recovery endpoint for ADMIN
