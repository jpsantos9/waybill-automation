package com.lazadaauto.service;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

@Service
public class WaybillService {
    public void generateWaybillAndDownload(WebDriver driver, String url) throws Exception {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        driver.get(url);
        wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//span[normalize-space()='Print AWB']")
        ));

        // Close any popups/dialogs if present
        closeAllPopups(driver);

        // Click the checkbox for the order
        System.out.println("[Info] Selecting the order checkbox...");
        WebElement parent = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.next-affix-top")));
        WebElement label = parent.findElement(By.cssSelector("div.list-check-cell label.next-checkbox-wrapper"));
        label.click();
        Thread.sleep(1000);

        // Click the Print AWB button
        System.out.println("[Info] Clicking Print AWB button...");
        WebElement printAwbBtn = driver.findElement(By.xpath("//span[contains(text(),'Print AWB')]"));
        printAwbBtn.click();

        // Wait for a new tab/window to open and switch to the print page
        System.out.println("[Info] Waiting to switch to print tab...");
        boolean switched = switchToTabByUrlContains(driver, "/apps/order/print", 10000);

        // Print the title of the currently selected tab
        System.out.println("[Info] Selected tab title: " + driver.getTitle());
        if (switched) {
            // allow the print page to load
            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.tagName("iframe")
            ));
            // Try to open the direct PDF link via anchor, otherwise click button (including inside iframes)
            try {
                // First, check for print iframe(s) that directly embed the AWB PDF (example HTML shows an <iframe src="...logistics-waybill-oss-...">)
                System.out.println("[Info] Looking for AWB iframe or link...");
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
                    System.out.println("[Info] Found AWB iframe src: " + pdfSrc);
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
                }
            } catch (Exception e) {
                System.out.println("[Error] No direct AWB iframe/link found, trying Print button...");
                e.printStackTrace();
            }
        } else {
            System.out.println("[Error] Failed to switch to print tab.");
        }
        Thread.sleep(2000); // Wait for file to download
    }

    private void closeAllPopups(WebDriver driver) {
        By closeBtnLocator = By.cssSelector("a.asc-tour-helper-close");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

        boolean popupsRemaining = true;

        while (popupsRemaining) {
            try {
                // Wait until at least one close button is present
                wait.until(ExpectedConditions.presenceOfElementLocated(closeBtnLocator));

                // Get all currently visible close buttons
                List<WebElement> closeBtns = driver.findElements(closeBtnLocator);

                if (closeBtns.isEmpty()) {
                    popupsRemaining = false;
                    break;
                }

                for (WebElement btn : closeBtns) {
                    int attempts = 0;
                    boolean clicked = false;

                    while (attempts < 3 && !clicked) {
                        try {
                            btn.click();
                            Thread.sleep(300); // small pause between clicks
                            clicked = true;
                        } catch (StaleElementReferenceException | ElementClickInterceptedException | InterruptedException e) {
                            System.out.println("[Error] Retrying click for a close button");
                            attempts++;
                            // Re-find the button for next attempt
                            List<WebElement> refreshedBtns = driver.findElements(closeBtnLocator);
                            if (!refreshedBtns.isEmpty()) {
                                btn = refreshedBtns.get(0); // pick first available
                            }
                        }
                    }
                }

            } catch (TimeoutException e) {
                // No more popups found
                popupsRemaining = false;
            }
        }

        System.out.println("[Info] All popups closed.");
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
                        } catch (Exception e) {
                            System.out.println("[Error] Error switching to window handle: " + handle);
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("[Error] Error getting window handles.");
                e.printStackTrace();
            }
        }
        return false;
    }
}
