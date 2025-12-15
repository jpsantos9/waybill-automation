package com.lazadaauto.controller;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lazadaauto.service.PdfMergeService;

@RestController
@RequestMapping("/api")
public class PdfMergeController {

    @Autowired
    private PdfMergeService pdfMergeService;

    @PostMapping(path = "/merge-pdfs", produces = "application/pdf")
    public ResponseEntity<?> mergePdfs() {
        try {
            List<File> files = new ArrayList<>();
            File downloads = new File("C:\\Downloads");
            if (!downloads.exists() || !downloads.isDirectory()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Downloads folder not found: C:\\Downloads");
            }

            File[] list = downloads.listFiles((d, name) -> name.toLowerCase().endsWith(".pdf"));
            if (list != null && list.length > 0) {
                Arrays.sort(list, Comparator.comparingLong(File::lastModified).reversed());
                for (File f : list) files.add(f);
            }

            if (files.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No PDF files found to merge");
            }

            File out = pdfMergeService.compileFourToOne(files);
            if (out == null || !out.exists()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create merged PDF");
            }

            byte[] data = Files.readAllBytes(out.toPath());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", out.getName());
            headers.setContentLength(data.length);

            return new ResponseEntity<>(data, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error merging PDFs: " + e.getMessage());
        }
    }

    @DeleteMapping(path = "/clear-downloads", produces = "application/json")
    public ResponseEntity<?> clearDownloads() {
        try {
            int deleted = pdfMergeService.clearDownloads();
            return ResponseEntity.ok(java.util.Collections.singletonMap("deleted", deleted));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error clearing downloads: " + e.getMessage());
        }
    }
}
