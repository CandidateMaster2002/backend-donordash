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
        receipt.setDonationId("12345");
        receipt.setReceiptNumber("REC-2026-001");
        receipt.setPaymentDate("2026-11-10");
        receipt.setAmount(5100.0);
        receipt.setDonorName("Radha Krishna Das");
        receipt.setDonorAddress("108 Temple Street");
        receipt.setCity("Dhanbad");
        receipt.setState("Jharkhand");
        receipt.setDonorPIN("826001");
        receipt.setPan("ABCDE1234F");
        receipt.setMobile("9876543210");
        receipt.setEmail("devotee@example.com");
        receipt.setPaymentMode("UPI");
        receipt.setTransactionID("UPI/20261110/998877");
        receipt.setPurpose("General Donation");
        receipt.setDonorCultivatorId("2");

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

