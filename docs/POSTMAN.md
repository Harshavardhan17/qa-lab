# Postman guide

## Import

1. Open Postman → **Import** → drag in both files from `postman/`:
   - `BankQALab.postman_collection.json`
   - `BankQALab.postman_environment.json`
2. Top-right environment dropdown → select **Bank QA Lab (local)**.
3. Make sure the app is running (`docker compose up -d`). `baseUrl` defaults to `http://localhost:8080`.

## How auth works here

- The API is stateless and uses **JWT bearer tokens**.
- Run **`1. Auth > Login (alice - customer)`**. Its *Tests* tab saves `accessToken` and
  `refreshToken` into the environment automatically.
- Every other request inherits `Authorization: Bearer {{accessToken}}` from the collection.
- For ADMIN-only endpoints, run **`Login (admin)`** to replace the token.
- When the access token expires (60 min), run **`Refresh access token`**.

## Suggested run order

1. `1. Auth` → Login (alice) → Get current user (`/me`).
2. `3. Accounts` → List my accounts → Open account (saves `accountId`) → Deposit → Withdraw →
   Account statement.
3. `5. Beneficiaries` → Add beneficiary (saves `beneficiaryId`).
4. `4. Transfers` → Create internal transfer (saves `transferId`) → Get transfer → List transfers.
5. `6. Cards` → Issue card → Block → Update limit.
6. `7. Loans` → Apply (saves `loanId`) → (login admin) Approve → (login alice) Repay.
7. `2. Customers` → login admin → List/Create/PUT/PATCH/Delete.
8. `9. Error & auth demos` → see 400 / 401 / 403 / 404 / 422 responses on purpose.

## HTTP methods & concepts covered

| Method | Example request | Teaches |
|--------|-----------------|---------|
| `GET` (list) | List accounts, List customers | query params, filtering, pagination (`page`,`size`,`sort`) |
| `GET` (by id) | Get account / transfer / loan | path params, 404 handling |
| `POST` | Login, Open account, Transfer, Apply loan | request bodies, 201 Created, token capture |
| `PUT` | Update customer / beneficiary | full replace semantics |
| `PATCH` | Patch customer, Freeze account, Card limit | partial update semantics |
| `DELETE` | Delete customer, Close account, Delete beneficiary | 204 No Content, 409 conflicts |
| auth | No-token, bad login | 401 Unauthorized |
| roles | Customer hitting admin list | 403 Forbidden |
| validation | Invalid register/customer body | 400 with `fieldErrors[]` |
| business rules | Withdraw over balance, transfer with two destinations | 422 Unprocessable Entity |

## Full endpoint reference

Auth (public): `POST /api/auth/register`, `POST /api/auth/login`, `POST /api/auth/refresh`,
`GET /api/auth/me`.

Customers (ADMIN to list/create/delete): `GET /api/customers`, `GET/PUT/PATCH/DELETE /api/customers/{id}`,
`POST /api/customers`.

Accounts: `GET /api/accounts`, `GET /api/accounts/{id}`, `POST /api/accounts`,
`GET /api/accounts/{id}/transactions`, `POST /api/accounts/{id}/deposit`,
`POST /api/accounts/{id}/withdraw`, `PATCH /api/accounts/{id}/status`, `DELETE /api/accounts/{id}`.

Transfers: `POST /api/transfers`, `GET /api/transfers`, `GET /api/transfers/{id}`,
`POST /api/transfers/{id}/reverse` (ADMIN).

Beneficiaries: `GET/POST /api/beneficiaries`, `GET/PUT/DELETE /api/beneficiaries/{id}`.

Cards: `GET/POST /api/cards`, `GET /api/cards/{id}`, `PATCH /api/cards/{id}/block`,
`PATCH /api/cards/{id}/limit`.

Loans: `GET /api/loans`, `GET /api/loans/{id}`, `POST /api/loans/apply`,
`POST /api/loans/{id}/approve` (ADMIN), `POST /api/loans/{id}/reject` (ADMIN),
`POST /api/loans/{id}/repay`.

Branches (public): `GET /api/branches`, `GET /api/branches/{id}`.

> Tip: the same endpoints are explorable interactively at **Swagger UI**
> (`http://localhost:8080/swagger-ui.html`) — click **Authorize**, paste your `accessToken`, and
> "Try it out".
