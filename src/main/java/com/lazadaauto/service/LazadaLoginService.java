package com.lazadaauto.service;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LazadaLoginService {

    @Value("${lazada.username}")
    private String username;

    @Value("${lazada.password}")
    private String password;

    public boolean login(WebDriver driver) throws Exception {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get("https://sellercenter.lazada.com.ph/apps/seller/login?spm=a1zawj.15023480.sign_up_container.d_local_login.24357a7eoYD8SB&login=1");
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//p[normalize-space()='Login with Password']")
        ));

        WebElement usernameField = driver.findElement(By.id("account"));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button.login-button"));
        usernameField.sendKeys(username);
        passwordField.sendKeys(password);
        loginButton.click();
        
        boolean isElementPresent;
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//span[normalize-space()='Oliver Plastic Center']")
            ));
            System.out.println("Lazada login successful");
            isElementPresent = true;
        } catch (TimeoutException e) {
            System.out.println("Lazada login failed or took too long");
            isElementPresent = false;
        }
        return isElementPresent;
    }
}
