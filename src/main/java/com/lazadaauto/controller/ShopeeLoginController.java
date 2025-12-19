package com.lazadaauto.controller;

import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lazadaauto.service.ShopeeLoginService;

@RestController
@RequestMapping("/api")
public class ShopeeLoginController {

    @Autowired
    private ShopeeLoginService shopeeLoginService;

    @Value("${app.chrome.auto-close:true}")
    private boolean autoClose;

    @GetMapping("/login-shopee")
    public ResponseEntity<String> loginShopee() {
        ChromeOptions options = new ChromeOptions();
        options.setUnhandledPromptBehaviour(UnexpectedAlertBehaviour.DISMISS_AND_NOTIFY);
        options.addArguments("--start-maximized");
        WebDriver driver = new ChromeDriver(options);
        try {
            boolean ok = shopeeLoginService.login(driver);
            if (ok) return ResponseEntity.ok("Shopee login successful");
            else return ResponseEntity.status(401).body("Shopee login failed");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        } finally {
            if (autoClose) driver.quit();
        }
    }
}
