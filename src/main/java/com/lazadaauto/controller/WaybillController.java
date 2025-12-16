package com.lazadaauto.controller;

import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lazadaauto.service.LazadaLoginService;
import com.lazadaauto.service.WaybillService;
import com.lazadaauto.util.WebDriverFactory;

@RestController
@RequestMapping("/api")
public class WaybillController {

    @Autowired
    private LazadaLoginService loginService;
    
    @Autowired
    private WaybillService waybillService;

    @Autowired
    private PdfMergeController pdfMergeController;

    @Autowired
    private WebDriverFactory webDriverFactory;

    @GetMapping("/lazada/generate-handover-waybill")
    public ResponseEntity<?> generateHandoverWaybill() {
        return generateWaybill("https://sellercenter.lazada.com.ph/apps/order/list?oldVersion=1&spm=a1zawj.portal_home.navi_left_sidebar.droot_normal_rp_asc_v2_ordersreviews_rp_asc_v2_ordersnewui.15bc1e13ZFZh2D&status=toshiphandover");
    }

    @GetMapping("/lazada/generate-toship-waybill")
    public ResponseEntity<?> generateToshipWaybill() {
        return generateWaybill("https://sellercenter.lazada.com.ph/apps/order/list?oldVersion=1&spm=a1zawj.portal_home.navi_left_sidebar.droot_normal_rp_asc_v2_ordersreviews_rp_asc_v2_ordersnewui.15bc1e13OwhbvT&status=toshiparrangeshipment");
    }

    public ResponseEntity<?> generateWaybill(String url) {
        WebDriver driver = webDriverFactory.createDriver();
        try {
            boolean loggedIn = loginService.login(driver);
            if (!loggedIn) {
                return ResponseEntity.status(401).build();
            }

            // generate and download waybill
            waybillService.generateWaybillAndDownload(driver, url);

            // Merge downloaded PDFs
            ResponseEntity<?> mergeResponse = pdfMergeController.mergePdfs();
            pdfMergeController.clearDownloads();
            return mergeResponse;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } finally {
            driver.quit();
        }
    }
}
