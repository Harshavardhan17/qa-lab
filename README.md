# Bank QA Lab

A self-contained **banking REST API + web UI** for learning QA. It runs entirely in Docker
(PostgreSQL + a Spring Boot Java backend), seeds realistic data on first start, and ships with a
ready-to-import **Postman collection**. Once you're comfortable testing the API by hand and
verifying results in the database, you write **Selenium** tests against the same app — all in one
project.

Built for a QA engineer who wants to:

1. **Learn REST + Postman** — every HTTP method (GET/POST/PUT/PATCH/DELETE), path/query params,
   request-body combinations, JWT auth flows, validation and error responses.
2. **Verify results in PostgreSQL** — a browser DB viewer (Adminer) is included, or use any SQL client.
3. **Automate the UI with Selenium** — the bundled web pages have stable, documented locators, and
   two example Selenium tests you can grow.

Low laptop load by design: slim Alpine images, a memory-capped JVM, and a small dataset.

---

## 1. Prerequisites

- **Docker Desktop** (Windows/macOS/Linux). That's the only requirement to run the app.
  - Verify: `docker --version` and `docker compose version`.
- **Optional — only for writing/running Selenium tests locally:** JDK 21 and Google Chrome.
  (The API and DB do **not** need Java installed on your machine — it's inside Docker.)

## 2. Quick start (one command)

```bash
docker compose up -d --build
```

The first build downloads dependencies and takes a few minutes; later starts take seconds.
When it's ready (about 20–30s after start), open:

| What | URL | Notes |
|------|-----|-------|
| REST API base | http://localhost:8080/api | Use from Postman |
| Swagger UI (interactive API docs) | http://localhost:8080/swagger-ui.html | Click **Authorize**, paste a token |
| Web UI (Selenium target) | http://localhost:8080/login | Log in as a customer |
| Adminer (DB browser) | http://localhost:8081 | System **PostgreSQL**, Server **db**, user/pass/db all `bankqa` |
| PostgreSQL | `localhost:5432` | db/user/pass = `bankqa` |
| Health check | http://localhost:8080/actuator/health | Should say `UP` |

Check status any time:

```bash
docker compose ps
docker compose logs -f app     # watch the app log (incl. the "[seed] done ..." summary)
```

Stop / reset:

```bash
docker compose down            # stop, keep the database
docker compose down -v         # stop AND wipe the database (re-seeds fresh next start)
```

## 3. Demo logins

All customers share the password **`Password123!`**. The admin password is **`admin123`**.

| Username | Role | Password |
|----------|------|----------|
| `admin` | ADMIN | `admin123` |
| `alice`, `bob`, `carol`, `dave`, `eve`, `frank`, `grace`, `heidi` | CUSTOMER | `Password123!` |

`ADMIN` can list all customers, approve/reject loans and reverse transfers. A `CUSTOMER` only sees
and manages their own data.

## 4. Seeded data (deterministic)

Generated with a fixed random seed, so every fresh environment is identical — great for repeatable
scripts. On a clean start you get: **5 branches, 9 logins, 8 customers, 12 accounts, ~160
transactions, several transfers, 4 beneficiaries, 7+ cards, 5 loans**.

Handy known values:

| Account # | Owner | Type |
|-----------|-------|------|
| `AC1000000001` | alice | SAVINGS |
| `AC1000000002` | alice | CHECKING |
| `AC1000000003` | bob | SAVINGS |
| … | … | sequential to `AC1000000012` |

## 5. Using the Postman collection

Import both files from the `postman/` folder into Postman:

- `BankQALab.postman_collection.json`
- `BankQALab.postman_environment.json` (select it as the active environment)

Then **run `1. Auth > Login (alice - customer)` first** — it captures your `accessToken`
automatically, and every other request reuses it. For ADMIN-only calls, run `Login (admin)`.

Full walkthrough and suggested order: **[docs/POSTMAN.md](docs/POSTMAN.md)**.

## 6. Verifying results in PostgreSQL

Open **http://localhost:8081** (Adminer) — no install needed — or connect any SQL client to
`localhost:5432` (`bankqa`/`bankqa`/`bankqa`). Example: after a transfer, confirm two ledger rows
share the same reference. Sample queries and the schema map: **[docs/DATABASE.md](docs/DATABASE.md)**.

## 7. Writing Selenium tests

The web UI at `/login`, `/dashboard`, `/accounts`, `/transfer`, `/beneficiaries` is built for
automation — every field/table/button has a stable `id` or `class`. Two example tests live in
`src/test/java/com/bank/qalab/ui/`.

With the app running and Chrome + JDK 21 installed:

```bash
./gradlew uiTest                 # runs the Selenium suite (headless by default)
./gradlew uiTest -Dselenium.headless=false   # watch the browser
```

Locator reference and how to add tests: **[docs/SELENIUM.md](docs/SELENIUM.md)**.

## 8. A suggested learning path

Manual API testing → DB verification → automation. Step-by-step curriculum:
**[docs/LEARNING-PATH.md](docs/LEARNING-PATH.md)**.

## 9. Project layout

```
bank-qa-lab/
├── docker-compose.yml         # db + app + adminer
├── Dockerfile                 # multi-stage build of the Spring Boot jar
├── build.gradle / gradlew     # Gradle wrapper (no local Gradle needed)
├── postman/                   # collection + environment
├── docs/                      # POSTMAN / DATABASE / SELENIUM / LEARNING-PATH guides
└── src/
    ├── main/java/com/bank/qalab/
    │   ├── domain/            # JPA entities (Customer, Account, Transaction, ...)
    │   ├── repo/              # Spring Data repositories
    │   ├── dto/               # request/response records
    │   ├── service/           # business logic (transfers, loans, ...)
    │   ├── security/          # JWT + user details
    │   ├── config/            # security chains, OpenAPI
    │   ├── web/api/           # REST controllers (/api/**)
    │   ├── web/ui/            # Thymeleaf MVC controllers
    │   └── seed/              # startup data seeder
    ├── main/resources/        # application.yml, templates/, static/
    └── test/java/com/bank/qalab/
        ├── (unit + context tests)
        └── ui/                # Selenium tests (@Tag("ui"))
```

## 10. Tech stack

Java 21 · Spring Boot 3.3 (Web, Data JPA, Security, Validation, Thymeleaf, Actuator) ·
PostgreSQL 16 · JWT (jjwt) · springdoc OpenAPI/Swagger · Selenium 4 · Gradle 8.10 · Docker Compose.

## 11. Configuration & ports

The app reads environment variables (see `docker-compose.yml`). Override them via a `.env` file in
this folder (copy `.env.example`) or your shell. Most useful:

| Variable | Default | Purpose |
|----------|---------|---------|
| `APP_PORT` | `8080` | Host port for the API/UI/Swagger |
| `ADMINER_PORT` | `8081` | Host port for Adminer |
| `DB_PORT` | `5432` | Host port for PostgreSQL |
| `JWT_SECRET` | demo value | HMAC signing key (≥ 32 chars) |
| `SEED_ENABLED` | `true` | Seed demo data on first start |

If a port is already in use, e.g. change it:

```bash
APP_PORT=9090 ADMINER_PORT=9091 docker compose up -d
```

## 12. Building/testing without Docker (optional)

```bash
./gradlew build     # compiles + runs unit and in-memory (H2) context tests; no Docker needed
./gradlew bootRun   # runs the app locally (needs a PostgreSQL on localhost:5432, or start `db` via compose)
```

## 13. Troubleshooting

- **`port is already allocated`** → change `APP_PORT`/`ADMINER_PORT`/`DB_PORT` (section 11).
- **App keeps restarting / can't connect to DB** → `docker compose logs app`; ensure `db` is healthy
  (`docker compose ps`). The app waits for the DB health check automatically.
- **Want fresh data** → `docker compose down -v && docker compose up -d`.
- **401 from the API** → your token expired or is missing; re-run the Postman **Login** request.
- **Selenium can't start a browser** → install Google Chrome; Selenium 4 fetches the driver itself.
# qa-lab
