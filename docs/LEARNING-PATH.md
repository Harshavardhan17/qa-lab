# QA learning path

A staged route from "never used Postman" to "writing automated tests". Do the stages in order;
each builds on the previous one. Everything runs against the local app (`docker compose up -d`).

## Stage 0 — Get it running
- Start the stack, open Swagger UI, log in to the web UI as `alice`, open Adminer.
- Goal: all four URLs in the README table load.

## Stage 1 — Explore the API (Swagger)
- Open `http://localhost:8080/swagger-ui.html`.
- `POST /api/auth/login` with `alice`/`Password123!`, copy `accessToken`, click **Authorize**.
- Try `GET /api/accounts` and `GET /api/branches`.
- Goal: understand endpoints, request/response shapes and status codes.

## Stage 2 — Read data with Postman (GET)
- Import the collection + environment. Run **Login (alice)**.
- Run `List my accounts`, `Get account by id`, `Account statement`.
- Experiment with query params: `?status=ACTIVE`, `?size=5&sort=balance,desc`.
- Goal: comfortable with headers, path params, query params, pagination.

## Stage 3 — Change data (POST / PUT / PATCH / DELETE)
- `Open account` (POST) → `Deposit` → `Withdraw`.
- `Add beneficiary` (POST), `Update beneficiary` (PUT), then delete it (DELETE).
- Log in as `admin`; `Create customer` (POST), `PUT` full update, `PATCH` partial update.
- Goal: understand the difference between POST vs PUT vs PATCH, and 201/200/204 responses.

## Stage 4 — Auth flows & error handling
- From `9. Error & auth demos`: call an endpoint with **no token** (401), a **bad login** (401),
  an **unknown id** (404), a **customer hitting the admin list** (403).
- Send an invalid body (empty fields) → 400 with a `fieldErrors` list.
- Withdraw more than the balance → 422 business error.
- Run `Refresh access token` to get a fresh access token.
- Goal: recognise and explain 400 vs 401 vs 403 vs 404 vs 422, and the JWT access/refresh cycle.

## Stage 5 — Verify results in the database
- After each write in Postman, confirm it in Adminer / SQL (see `DATABASE.md`).
- Prove a transfer created two matching ledger rows; prove a deposit changed the balance.
- Goal: never trust the API response alone — verify persistence.

## Stage 6 — An end-to-end scenario
Design and execute one flow, verifying in the DB at each step:
1. Log in as `alice`.
2. Open a new SAVINGS account with a $500 initial deposit.
3. Deposit $250, withdraw $100 → expect balance $650.
4. Add a beneficiary, then transfer $25 to `AC1000000002`.
5. In the DB: confirm the balance, the transactions, and the transfer rows.
- Goal: chain requests using captured variables and validate the whole journey.

## Stage 7 — Automate the UI with Selenium
- Read `SELENIUM.md`. Run `./gradlew uiTest` and watch it with `-Dselenium.headless=false`.
- Recreate your Stage 6 scenario as a Selenium test: log in, open `/transfer`, submit, assert the
  success banner, then (optionally) verify in the DB.
- Goal: turn a manual test case into a repeatable automated one — in the same project.

## Where to go next
- Add negative Selenium tests (e.g. transfer more than the balance → `#errorAlert`).
- Parameterise tests across the seeded customers.
- Add API-level automated tests (Postman + `newman`, or REST-assured) and compare with UI tests.
