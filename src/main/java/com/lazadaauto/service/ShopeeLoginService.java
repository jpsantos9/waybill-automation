package com.lazadaauto.service;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ShopeeLoginService {

    @Value("${shopee.username}")
    private String username;

    @Value("${shopee.password}")
    private String password;

    public boolean login(WebDriver driver) {
        try {
            driver.get("https://accounts.shopee.ph/seller/login?next=https%3A%2F%2Fseller.shopee.ph%2F");
            Thread.sleep(2000);

            WebElement userField = null;
            String[] userSelectors = {"input#loginKey","input[name='loginKey']","input[name='login_id']","input[name='email']","input[type='email']","input[name='username']"};
            for (String sel : userSelectors) {
                try {
                    userField = driver.findElement(By.cssSelector(sel));
                    if (userField != null) break;
                } catch (Exception ignored) {}
            }

            WebElement passField = null;
            String[] passSelectors = {"input[type='password']","input[name='password']","input#password"};
            for (String sel : passSelectors) {
                try {
                    passField = driver.findElement(By.cssSelector(sel));
                    if (passField != null) break;
                } catch (Exception ignored) {}
            }

            if (userField == null || passField == null) {
                return false;
            }

            userField.clear();
            userField.sendKeys(username);
            passField.clear();
            passField.sendKeys(password);

            // Try to submit the form
            try {
                WebElement submit = driver.findElement(By.xpath("//button[(contains(normalize-space(.),'Log In') or contains(@class,'ZzzLTG') or @type='submit')]"));
                submit.click();
            } catch (Exception e) {
                try { passField.submit(); } catch (Exception ignored) {}
            }

            Thread.sleep(4000);
            boolean loggedIn = driver.getCurrentUrl().contains("seller.shopee.ph/?is_from_login=true");
            return loggedIn;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
