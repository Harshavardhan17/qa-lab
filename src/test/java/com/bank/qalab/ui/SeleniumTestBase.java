package com.bank.qalab.ui;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Shared Selenium setup. The app must already be running (start it with `docker compose up`).
 *
 * Config via system properties or env vars:
 *   -Dapp.base.url / APP_BASE_URL         (default http://localhost:8080)
 *   -Dselenium.headless / SELENIUM_HEADLESS (default true)
 *
 * Selenium 4 downloads the matching ChromeDriver automatically (Selenium Manager),
 * so you only need Google Chrome installed.
 */
public abstract class SeleniumTestBase {

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = prop("app.base.url", "APP_BASE_URL", "http://localhost:8080");
        boolean headless = Boolean.parseBoolean(prop("selenium.headless", "SELENIUM_HEADLESS", "true"));

        ChromeOptions options = new ChromeOptions();
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu", "--window-size=1280,900");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /** Logs in through the form and waits for the dashboard. */
    protected void login(String username, String password) {
        driver.get(baseUrl + "/login");
        driver.findElement(By.id("username")).sendKeys(username);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.id("loginBtn")).click();
        wait.until(ExpectedConditions.urlContains("/dashboard"));
    }

    private static String prop(String sysProp, String envVar, String fallback) {
        String v = System.getProperty(sysProp);
        if (v == null || v.isBlank()) v = System.getenv(envVar);
        return (v == null || v.isBlank()) ? fallback : v;
    }
}
