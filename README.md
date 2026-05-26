# PennyWise Server

Spring Boot backend for the PennyWise migration.

## Stack

- Java 17
- Spring Boot 3
- Spring Web
- Spring Security with JWT
- Spring Data JPA
- PostgreSQL
- JJWT (JSON Web Token library)

## Setup

1. Create a PostgreSQL database named `pennywise` with user `pennywise` and password `pennywise`.
2. Optionally override environment variables:
   - `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` for database connection
   - `JWT_SECRET` for token signing (keep this secure in production)
   - `JWT_EXPIRATION` for access token lifetime (default: 1 hour)
   - `JWT_REFRESH_EXPIRATION` for refresh token lifetime (default: 7 days)

## Run

```bash
mvn spring-boot:run
```

## API Endpoints

### Health

- `GET /api/v1/health` - Server health check

### Auth

- `POST /api/v1/auth/sign-up` - Register new user
- `POST /api/v1/auth/sign-in` - Login user
- `POST /api/v1/auth/refresh` - Refresh access token
- `GET /api/v1/auth/me` - Get current user profile

### Budget (Protected)

- `POST /api/v1/budgets` - Create budget
- `GET /api/v1/budgets` - Get user's budgets
- `GET /api/v1/budgets/{id}` - Get specific budget
- `PUT /api/v1/budgets/{id}` - Update budget
- `DELETE /api/v1/budgets/{id}` - Delete budget

### Expense (Protected)

- `POST /api/v1/expenses` - Create expense
- `GET /api/v1/expenses` - Get user's expenses
- `GET /api/v1/expenses/{id}` - Get specific expense
- `GET /api/v1/expenses/budget/{budgetId}` - Get expenses by budget
- `PUT /api/v1/expenses/{id}` - Update expense
- `DELETE /api/v1/expenses/{id}` - Delete expense

### Dashboard (Protected)

- `GET /api/v1/dashboard` - Get dashboard summary, charts, and metrics

### Subscription (Protected)

- `GET /api/v1/subscription/plans` - Get available subscription plans
- `GET /api/v1/subscription` - Get user's current subscription
- `POST /api/v1/subscription` - Create or upgrade subscription
- `DELETE /api/v1/subscription` - Cancel subscription

## Architecture

- **JwtAuthenticationFilter**: Validates JWT tokens on every request
- **AuthService**: Handles user registration, login, and token refresh
- **User entity**: Stores user credentials and metadata in PostgreSQL
- **@AuthenticatedUser**: Custom annotation to inject the current user ID into controller methods

## Authorization Scheme

- Public endpoints: health, sign-up, sign-in, refresh
- Protected endpoints: All other endpoints require a valid JWT in the `Authorization: Bearer <token>` header
- User isolation: Each user can only access their own data (enforced in business logic)

## Modules Implemented

- **Auth Module**: JWT-based authentication with sign-up, sign-in, refresh, and profile endpoints
- **Budget Module**: Full CRUD for budgets with user isolation
- **Expense Module**: Full CRUD for expenses with budget assignment
- **Dashboard Module**: Aggregates budget and expense data for dashboard visualization
- **Subscription Module**: Manages subscription plans and upgrades

## Monthly Storage

- Budget and expense records include a `billingMonth` value in `YYYY-MM` format.
- A scheduled archive job runs at the start of each month and moves the previous month's budgets and expenses into month-specific archive tables.
- This keeps the active tables focused on the current month while preserving historical data separately.

## Error Handling

All endpoints return consistent error responses with:

- `timestamp` - when error occurred
- `status` - HTTP status code
- `error` - error type
- `message` - human-readable message
- `code` - machine-readable error code (for TOKEN_EXPIRED, INVALID_TOKEN, etc.)

## Next Steps

- Add Flyway migrations for schema initialization
- Add Category entity and endpoints for expense categorization
- Add audit logging for sensitive operations
- Add pagination/filtering for list endpoints
- Add integration tests
- Deploy to production environment
