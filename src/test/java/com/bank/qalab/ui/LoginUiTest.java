package com.bank.qalab.ui;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("ui")
class LoginUiTest extends SeleniumTestBase {

    @Test
    void customerCanLogInAndSeeTheirAccounts() {
        login("alice", "Password123!");

        WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("pageTitle")));
        assertEquals("Dashboard", title.getText());
        assertEquals("alice", driver.findElement(By.id("currentUser")).getText());
        assertFalse(driver.findElements(By.cssSelector(".accountRow")).isEmpty(),
                "seeded customer should have at least one account row");
    }

    @Test
    void invalidPasswordShowsError() {
        driver.get(baseUrl + "/login");
        driver.findElement(By.id("username")).sendKeys("alice");
        driver.findElement(By.id("password")).sendKeys("wrong-password");
        driver.findElement(By.id("loginBtn")).click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginError")));
        assertTrue(error.getText().toLowerCase().contains("invalid"));
    }
}
