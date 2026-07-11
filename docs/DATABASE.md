# Verifying results in PostgreSQL

A core QA skill: after calling an API, confirm the database actually changed the way you expect.

## Ways to connect

**Option A — Adminer (browser, nothing to install)**
Open **http://localhost:8081** and log in:

| Field | Value |
|-------|-------|
| System | PostgreSQL |
| Server | `db` |
| Username | `bankqa` |
| Password | `bankqa` |
| Database | `bankqa` |

**Option B — any SQL client** (DBeaver, pgAdmin, DataGrip, `psql`):

```
Host: localhost   Port: 5432   Database: bankqa   User: bankqa   Password: bankqa
JDBC URL: jdbc:postgresql://localhost:5432/bankqa
```

**Option C — psql inside the container**

```bash
docker exec -it bankqa-db psql -U bankqa -d bankqa
```

## Table map

| Table | Holds |
|-------|-------|
| `branch` | bank branches (reference data) |
| `customer` | customer profiles |
| `app_user` | login accounts (username, bcrypt password, role, `customer_id`) |
| `account` | accounts (number, type, status, `balance`, `customer_id`, `branch_id`) |
| `account_transaction` | ledger movements (type, amount, `balance_after`, `reference`) |
| `transfer` | transfers (reference, from/to account, beneficiary, status) |
| `beneficiary` | saved payees |
| `card` | debit/credit cards |
| `loan` | loans (principal, rate, emi, outstanding, status) |

## Sample queries

```sql
-- All accounts with owner name and balance
select a.account_number, c.first_name, c.last_name, a.type, a.status, a.balance
from account a join customer c on c.id = a.customer_id
order by a.account_number;

-- Statement for one account (newest first)
select created_at, type, amount, balance_after, description, counterparty
from account_transaction
where account_id = (select id from account where account_number = 'AC1000000001')
order by created_at desc;

-- Verify a transfer wrote TWO matching ledger rows (internal transfer)
select t.reference, t.type, t.amount, t.counterparty, a.account_number
from account_transaction t join account a on a.id = t.account_id
where t.reference = (select reference from transfer
                     where to_account_id is not null and status = 'COMPLETED' limit 1)
order by t.type;

-- Loans and how much is still owed
select c.first_name, l.type, l.principal, l.interest_rate, l.emi_amount, l.outstanding, l.status
from loan l join customer c on c.id = l.customer_id;
```

## A typical "test then verify" loop

1. In Postman, deposit `250.50` into an account and note the response `balanceAfter`.
2. In the DB, run the statement query above and confirm the newest row is a `DEPOSIT` of `250.50`
   with a matching `balance_after`.
3. Do an internal transfer, then run the transfer-verification query and confirm one `TRANSFER_OUT`
   and one `TRANSFER_IN` row share the same `reference` and amount.

> Reset everything back to the seeded state at any time: `docker compose down -v && docker compose up -d`.
