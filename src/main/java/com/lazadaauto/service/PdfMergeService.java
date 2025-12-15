package com.lazadaauto.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

@Service
public class PdfMergeService {

    /**
     * Render up to 4 single-page PDFs into a single PDF page (2x2 layout) and save to C:\\Downloads
     * @param inputPdfs list of input PDF files (will use first page of each)
     * @return File pointing to the created combined PDF or null on error
     */
    public File compileFourToOne(List<File> inputPdfs) throws IOException {
        if (inputPdfs == null || inputPdfs.isEmpty()) return null;

        String outDir = "C:\\Downloads";
        File dir = new File(outDir);
        if (!dir.exists()) dir.mkdirs();

        String outName = "compiled_4in1_" + Instant.now().toEpochMilli() + ".pdf";
        File outFile = new File(dir, outName);

        try (PDDocument outDoc = new PDDocument()) {
            PDPageContentStream content = null;
            PDPage page = null;
            int placed = 0; // 0..3 per output page

            for (File f : inputPdfs) {
                if (f == null || !f.exists()) continue;
                try (PDDocument src = PDDocument.load(f)) {
                    PDFRenderer renderer = new PDFRenderer(src);
                    int srcPages = src.getNumberOfPages();
                    for (int p = 0; p < srcPages; p++) {
                        BufferedImage image = renderer.renderImageWithDPI(p, 150);

                        if (placed == 0) {
                            page = new PDPage(PDRectangle.A4);
                            outDoc.addPage(page);
                            content = new PDPageContentStream(outDoc, page);
                        }

                        PDRectangle media = page.getMediaBox();
                        float pageWidth = media.getWidth();
                        float pageHeight = media.getHeight();
                        float halfW = pageWidth / 2f;
                        float halfH = pageHeight / 2f;

                        // scale image to fit quarter page while preserving aspect ratio
                        float targetW = halfW - 20; // padding
                        float targetH = halfH - 20;

                        float imgW = image.getWidth();
                        float imgH = image.getHeight();
                        float scale = Math.min(targetW / imgW, targetH / imgH);
                        float drawW = imgW * scale;
                        float drawH = imgH * scale;

                        // compute x,y based on placed index: 0->top-left,1->top-right,2->bottom-left,3->bottom-right
                        float x = 0, y = 0;
                        switch (placed) {
                            case 0: // top-left
                                x = 10;
                                y = halfH + 10 + (halfH - drawH - 20) / 2f;
                                break;
                            case 1: // top-right
                                x = halfW + 10;
                                y = halfH + 10 + (halfH - drawH - 20) / 2f;
                                break;
                            case 2: // bottom-left
                                x = 10;
                                y = 10 + (halfH - drawH - 20) / 2f;
                                break;
                            default: // bottom-right
                                x = halfW + 10;
                                y = 10 + (halfH - drawH - 20) / 2f;
                                break;
                        }

                        PDImageXObject pdImage = LosslessFactory.createFromImage(outDoc, image);
                        content.drawImage(pdImage, x, y, drawW, drawH);

                        placed++;
                        if (placed == 4) {
                            content.close();
                            content = null;
                            placed = 0;
                        }
                    }
                } catch (Exception ex) {
                    // skip this file but continue with others
                }
            }

            if (content != null) content.close();

            outDoc.save(outFile);
        }

        return outFile.exists() ? outFile : null;
    }

    /**
     * Delete all files (non-recursive) inside C:\\Downloads. Returns number of files deleted.
     */
    public int clearDownloads() {
        String outDir = "C:\\Downloads";
        File dir = new File(outDir);
        if (!dir.exists() || !dir.isDirectory()) return 0;

        File[] files = dir.listFiles();
        int deleted = 0;
        if (files == null) return 0;
        for (File f : files) {
            try {
                if (f.isFile()) {
                    if (f.delete()) deleted++;
                }
            } catch (Exception e) {
                // ignore individual delete failures
            }
        }
        return deleted;
    }
}
