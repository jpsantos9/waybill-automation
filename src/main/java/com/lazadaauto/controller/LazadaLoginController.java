package com.lazadaauto.controller;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lazadaauto.service.LazadaLoginService;

@RestController
@RequestMapping("/api")
public class LazadaLoginController {

    @Autowired
    private LazadaLoginService loginService;

    @Value("${app.chrome.auto-close:true}")
    private boolean autoClose;

    @PostMapping("/login")
        public ResponseEntity<String> login() {
            WebDriver driver = new ChromeDriver();
            try {
                boolean success = loginService.login(driver);
                if (success) {
                    return ResponseEntity.ok("Login successful");
                } else {
                    return ResponseEntity.status(401).body("Login failed");
                }
            } catch (Exception ex) {
                return ResponseEntity.status(500).body("Error: " + ex.getMessage());
            } finally {
                if (autoClose) driver.quit();
            }
        }
}
