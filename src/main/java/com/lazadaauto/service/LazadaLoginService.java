package com.lazadaauto.service;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LazadaLoginService {

    @Value("${lazada.username}")
    private String username;

    @Value("${lazada.password}")
    private String password;

    public boolean login(WebDriver driver) throws Exception {
        driver.get("https://sellercenter.lazada.com.ph/apps/seller/login?spm=a1zawj.15023480.sign_up_container.d_local_login.24357a7eoYD8SB&login=1");
        Thread.sleep(2000); // Wait for page to load

        WebElement usernameField = driver.findElement(By.id("account"));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button.login-button"));
        usernameField.sendKeys(username);
        passwordField.sendKeys(password);
        loginButton.click();
        Thread.sleep(3000); // Wait for login to process

        return true;
    }
}
