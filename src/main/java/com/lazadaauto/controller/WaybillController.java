package com.lazadaauto.controller;

import java.util.HashMap;
import java.util.Map;

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

import com.lazadaauto.service.LazadaLoginService;
import com.lazadaauto.service.WaybillService;

@RestController
@RequestMapping("/api")
public class WaybillController {

    @Autowired
    private LazadaLoginService loginService;
    
    @Autowired
    private WaybillService waybillService;

    @Autowired
    private PdfMergeController pdfMergeController;

    @Value("${app.chrome.headless:true}")
    private boolean headless;

    @GetMapping("/generate-waybill")
    public ResponseEntity<?> generateWaybill() {
        ChromeOptions options = new ChromeOptions();
        options.setUnhandledPromptBehaviour(UnexpectedAlertBehaviour.DISMISS_AND_NOTIFY);
        options.addArguments("--start-maximized");
        // run Chrome in headless mode when enabled by configuration
        if (headless) {
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--disable-gpu");
        }
        // set download directory
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory", "C:\\Downloads");
        prefs.put("download.prompt_for_download", false);
        prefs.put("plugins.always_open_pdf_externally", true);
        options.setExperimentalOption("prefs", prefs);
        WebDriver driver = new ChromeDriver(options);
        try {
            boolean loggedIn = loginService.login(driver);
            if (!loggedIn) {
                return ResponseEntity.status(401).build();
            }
            java.io.File awbFile = waybillService.generateWaybillAndDownload(driver);

            return pdfMergeController.mergePdfs();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        } finally {
            driver.quit();
        }
    }
}
