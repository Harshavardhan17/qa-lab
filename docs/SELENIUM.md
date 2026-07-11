# Selenium guide

Once you've tested the API by hand, automate the **web UI** in the *same* project. The pages under
`/login`, `/dashboard`, `/accounts`, `/transfer`, `/beneficiaries` were built for automation: every
field, table and button has a stable `id`/`class`.

## Prerequisites (only for Selenium)

- **Google Chrome** installed (Selenium 4 downloads the matching driver itself — no manual
  chromedriver setup).
- **JDK 21** installed locally (to run Gradle). The API/DB still run in Docker.
- The app running: `docker compose up -d`.

## Run the example tests

```bash
# from the project root, with the app running:
./gradlew uiTest                                # headless (default)
./gradlew uiTest -Dselenium.headless=false      # watch the browser
./gradlew uiTest -Dapp.base.url=http://localhost:9090   # if you changed APP_PORT
```

`uiTest` is a dedicated task — the normal `./gradlew build` never launches a browser
(the Selenium tests are tagged `@Tag("ui")` and excluded).

Existing tests (`src/test/java/com/bank/qalab/ui/`):

- `LoginUiTest` — logs in as a customer and asserts the dashboard + account rows; also checks the
  invalid-password error.
- `TransferUiTest` — logs in, submits an internal transfer and asserts the success message.

## How authentication differs from the API

This is a useful lesson: the **web UI uses classic session + form login** (a `JSESSIONID` cookie),
while the **REST API uses stateless JWT**. Selenium drives the *UI* login form; it does not need a
JWT. Two separate Spring Security filter chains make this work (see `config/SecurityConfig.java`).

## Locator reference

| Page | Element | Locator |
|------|---------|---------|
| Login | username / password / submit | `#username`, `#password`, `#loginBtn` |
| Login | error banner | `#loginError` |
| Nav (all pages) | links | `#navDashboard`, `#navAccounts`, `#navTransfer`, `#navBeneficiaries` |
| Nav | current user / logout | `#currentUser`, `#navLogout` |
| Any page | flash messages | `#successAlert`, `#errorAlert` |
| Dashboard | title / totals | `#pageTitle`, `#totalBalance`, `#accountCount` |
| Dashboard/Accounts | table + rows | `#accountsTable`, `tr.accountRow` (each has `data-account-id`) |
| Account rows | cells | `.acctNumber`, `.acctType`, `.acctStatus`, `.acctBalance`, `a.accountLink` |
| Account detail | fields | `#accountNumber`, `#accountType`, `#accountStatus`, `#accountBalance` |
| Account detail | statement | `#txnTable`, `tr.txnRow`, `.txnType`, `.txnAmount` |
| Transfer | form controls | `#transferForm`, `#fromAccount`, `#toAccountNumber`, `#beneficiary`, `#amount`, `#description`, `#submitTransfer` |
| Beneficiaries | table / rows | `#beneficiaryTable`, `tr.benRow` (each has `data-ben-id`), `.benName`, `.benAccount` |
| Beneficiaries | add form | `#addBeneficiaryForm`, `#benName`, `#benAccount`, `#benBank`, `#benIfsc`, `#addBeneficiary` |
| Beneficiaries | delete | `button.deleteBen` |

## Add your own test

Extend `SeleniumTestBase` (it gives you `driver`, `wait`, `baseUrl` and a `login(user, pass)` helper)
and tag the class `@Tag("ui")`:

```java
@Tag("ui")
class BeneficiaryUiTest extends SeleniumTestBase {
    @Test
    void canAddBeneficiary() {
        login("alice", "Password123!");
        driver.get(baseUrl + "/beneficiaries");
        driver.findElement(By.id("benName")).sendKeys("New Payee");
        driver.findElement(By.id("benAccount")).sendKeys("AC1000000009");
        driver.findElement(By.id("addBeneficiary")).click();
        WebElement ok = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("successAlert")));
        org.junit.jupiter.api.Assertions.assertTrue(ok.getText().contains("added"));
    }
}
```

Then run `./gradlew uiTest`.
