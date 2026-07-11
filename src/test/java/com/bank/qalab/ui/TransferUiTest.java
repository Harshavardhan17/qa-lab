package com.bank.qalab.ui;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("ui")
class TransferUiTest extends SeleniumTestBase {

    @Test
    void customerCanTransferBetweenOwnAccounts() {
        login("alice", "Password123!");
        driver.get(baseUrl + "/transfer");

        // Source: Alice's savings account (seeded as AC1000000001).
        Select from = new Select(
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("fromAccount"))));
        from.getOptions().stream()
                .filter(o -> o.getText().contains("AC1000000001"))
                .findFirst()
                .ifPresent(o -> from.selectByVisibleText(o.getText()));

        // Destination: Alice's checking account (seeded as AC1000000002).
        driver.findElement(By.id("toAccountNumber")).sendKeys("AC1000000002");
        driver.findElement(By.id("amount")).sendKeys("10.00");
        driver.findElement(By.id("description")).sendKeys("Selenium test transfer");
        driver.findElement(By.id("submitTransfer")).click();

        WebElement success = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("successAlert")));
        assertTrue(success.getText().toLowerCase().contains("completed"),
                "expected a completed-transfer confirmation, got: " + success.getText());
    }
}
