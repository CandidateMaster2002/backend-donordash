package com.iskcondhanbad.donordash;

import com.iskcondhanbad.donordash.dto.ReceiptDto;
import com.iskcondhanbad.donordash.service.PdfReceiptService;
import com.iskcondhanbad.donordash.utils.NumberToWordsUtil;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class PdfReceiptServiceTest {

    @Test
    void testNumberToWordsUtil() {
        assertEquals("Five Thousand Rupees Only", NumberToWordsUtil.convertToWords(5000));
        assertEquals("One Lakh Rupees Only", NumberToWordsUtil.convertToWords(100000));
        assertEquals("Twenty Five Thousand Five Hundred Rupees Only", NumberToWordsUtil.convertToWords(25500));
        assertEquals("Zero Rupees Only", NumberToWordsUtil.convertToWords(0));
    }

    @Test
    void testGeneratePdfFromReceiptDto() {
        PdfReceiptService service = new PdfReceiptService();

        ReceiptDto receipt = new ReceiptDto();
        receipt.setDonationId("2328");
        receipt.setReceiptNumber("ISKDHN-SUM-000051");
        receipt.setPaymentDate("2026-04-28");
        receipt.setAmount(3501.0);
        receipt.setDonorName("Kush Milkesh Mistry");
        receipt.setDonorAddress("B-146, The Gold City, Near Dhaniyavi Chokdi, Tarsali Bypass NH-8, Tarsali");
        receipt.setCity("Vadodara");
        receipt.setState("Gujarat");
        receipt.setDonorPIN("390009");
        receipt.setPan("GZVPM9347A");
        receipt.setMobile("7984399849");
        receipt.setEmail("kushmistry2021@gmail.com");
        receipt.setPaymentMode("Bank Transfer");
        receipt.setTransactionID("648443097400");
        receipt.setPurpose("Temple Construction");
        receipt.setDonorCultivatorId("8");

        byte[] pdfBytes = service.generatePdfFromReceiptDto(receipt);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 1000, "PDF byte array should be non-trivial");

        // Save a sample PDF in target directory for quick visual inspection
        try {
            java.nio.file.Files.write(java.nio.file.Path.of("target", "sample_receipt.pdf"), pdfBytes);
        } catch (Exception ignored) {
        }

        // Verify PDF Magic header %PDF-
        String header = new String(pdfBytes, 0, 5, StandardCharsets.US_ASCII);
        assertEquals("%PDF-", header);

        // Verify page count is exactly 2 pages (Page 1 fits on page 1 without spillover, Page 2 is T&C)
        try {
            com.lowagie.text.pdf.PdfReader reader = new com.lowagie.text.pdf.PdfReader(pdfBytes);
            assertEquals(2, reader.getNumberOfPages(), "PDF must contain exactly 2 pages without vertical overflow");
            reader.close();
        } catch (Exception e) {
            fail("Failed to parse generated PDF: " + e.getMessage());
        }
    }
}

