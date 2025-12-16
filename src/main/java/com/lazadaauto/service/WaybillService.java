package com.lazadaauto.service;

import java.io.File;
import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

@Service
public class WaybillService {
    public File generateWaybillAndDownload(WebDriver driver, String url) throws Exception {
        driver.get(url);
        Thread.sleep(2000); // Wait for page to load

        // Close any popups/dialogs if present
        closeAllPopups(driver);

        // Click the checkbox for the order
        WebElement checkbox = driver.findElement(By.cssSelector("input.next-checkbox-input"));
        checkbox.click();
        Thread.sleep(1000);

        // Click the Print AWB button
        WebElement printAwbBtn = driver.findElement(By.xpath("//span[contains(text(),'Print AWB')]") );
        printAwbBtn.click();

        // Wait for a new tab/window to open and switch to the print page
        boolean switched = switchToTabByUrlContains(driver, "/apps/order/print", 10000);
        System.out.println("Switched to print tab: " + switched);
        // Print the title of the currently selected tab
        try {
            System.out.println("Selected tab title: " + driver.getTitle());
        } catch (Exception ex) {
            System.out.println("Unable to get tab title: " + ex.getMessage());
        }
        if (switched) {
            // allow the print page to load
            Thread.sleep(2000);
            // Try to open the direct PDF link via anchor, otherwise click button (including inside iframes)
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            try {
                // First, check for print iframe(s) that directly embed the AWB PDF (example HTML shows an <iframe src="...logistics-waybill-oss-...">)
                List<WebElement> frames = driver.findElements(By.tagName("iframe"));
                String pdfSrc = null;
                for (WebElement f : frames) {
                    try {
                        String src = f.getAttribute("src");
                        if (src != null && (src.contains("logistics-waybill") || src.contains("logistics-waybill-oss") || src.contains("oss"))) {
                            pdfSrc = src;
                            break;
                        }
                    } catch (Exception ignore) {}
                }
                if (pdfSrc != null) {
                    System.out.println("Found AWB iframe src: " + pdfSrc);
                    try {
                        ((JavascriptExecutor) driver).executeScript("window.open(arguments[0], '_blank');", pdfSrc);
                        long s = System.currentTimeMillis();
                        while (System.currentTimeMillis() - s < 5000 && driver.getWindowHandles().size() <= 1) {
                            Thread.sleep(200);
                        }
                    } catch (Exception jsEx) {
                        // fallback: try to open in same tab
                        driver.get(pdfSrc);
                        Thread.sleep(2000);
                    }
                } else {
                    // If no iframe PDF found, try anchor first (old logic)
                    WebElement anchor = wait.until(ExpectedConditions.presenceOfElementLocated(
                            By.cssSelector("a[href*='logistics-waybill-oss-sg'], a[href*='logistics-waybill']")));
                    String href = anchor.getAttribute("href");
                    System.out.println("Found AWB download link: " + href);
                    if (href != null && !href.isEmpty()) {
                        try {
                            ((JavascriptExecutor) driver).executeScript("window.open(arguments[0], '_blank');", href);
                            long s = System.currentTimeMillis();
                            while (System.currentTimeMillis() - s < 5000 && driver.getWindowHandles().size() <= 1) {
                                Thread.sleep(200);
                            }
                        } catch (Exception jsEx) {
                            anchor.click();
                            long s = System.currentTimeMillis();
                            while (System.currentTimeMillis() - s < 5000 && driver.getWindowHandles().size() <= 1) {
                                Thread.sleep(200);
                            }
                        }
                    } else {
                        throw new Exception("anchor href empty");
                    }
                }
            } catch (Exception e) {
                try {
                    WebElement openBtn = new WebDriverWait(driver, Duration.ofSeconds(5))
                            .until(ExpectedConditions.elementToBeClickable(By.id("open-button")));
                    openBtn.click();
                } catch (Exception e2) {
                    // attempt inside iframes
                    List<WebElement> iframes = driver.findElements(By.tagName("iframe"));
                    boolean clicked = false;
                    for (WebElement frame : iframes) {
                        try {
                            driver.switchTo().frame(frame);
                            WebElement openBtnInFrame = new WebDriverWait(driver, Duration.ofSeconds(3))
                                    .until(ExpectedConditions.elementToBeClickable(By.id("open-button")));
                            openBtnInFrame.click();
                            clicked = true;
                            driver.switchTo().defaultContent();
                            break;
                        } catch (Exception ignore) {
                            try { driver.switchTo().defaultContent(); } catch (Exception ex) {}
                        }
                    }
                    if (!clicked) {
                        throw new RuntimeException("Could not find AWB open link or button on print page.");
                    }
                }
            }
        } else {
            // fallback: small wait to allow download to start
            Thread.sleep(5000);
        }

        // Get the default download directory
        String downloadDir = System.getProperty("user.home") + "/Downloads";
        File dir = new File(downloadDir);
        File[] files = dir.listFiles();
        if (files != null) {
            File latestFile = null;
            long lastModified = Long.MIN_VALUE;
            for (File file : files) {
                if (file.isFile() && file.lastModified() > lastModified) {
                    lastModified = file.lastModified();
                    latestFile = file;
                }
            }
            return latestFile;
        }
        return null;
    }

    private void closeAllPopups(WebDriver driver) {
        List<WebElement> closeBtns = driver.findElements(By.cssSelector("a.asc-tour-helper-close"));
        System.out.println("Found " + closeBtns.size() + " close buttons.");
        for (WebElement btn : closeBtns) {
            try {
                btn.click();
                Thread.sleep(500);
            } catch (Exception ignore) {
                // If not clickable, continue
            }
        }
    }

    private boolean switchToTabByUrlContains(WebDriver driver, String urlContains, int timeoutMillis) {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeoutMillis) {
            try {
                java.util.Set<String> handles = driver.getWindowHandles();
                if (handles.size() > 1) {
                    for (String handle : handles) {
                        try {
                            driver.switchTo().window(handle);
                            String current = driver.getCurrentUrl();
                            if (current != null && current.contains(urlContains)) {
                                return true;
                            }
                        } catch (Exception ignored) {
                            // continue trying other handles
                        }
                    }
                }
            } catch (Exception ignored) {}
            try { Thread.sleep(250); } catch (InterruptedException ignored) {}
        }
        return false;
    }
}
