package com.iskcondhanbad.donordash.service;

import com.iskcondhanbad.donordash.dto.ReceiptDto;
import com.iskcondhanbad.donordash.utils.NumberToWordsUtil;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@Service
public class PdfReceiptService {

    @Autowired
    private DonationService donationService;

    // Palette matching frontend styling
    private static final Color ISKCON_BLUE = new Color(2, 62, 125);
    private static final Color ACCENT_PINK = new Color(255, 54, 171);
    private static final Color DONOR_COPY_PINK = new Color(255, 135, 171);
    private static final Color LABEL_PINK = new Color(255, 10, 84);
    private static final Color YELLOW_BG = new Color(255, 243, 176);
    private static final Color BORDER_RED = new Color(208, 0, 0);
    private static final Color LIGHT_GRAY_BG = new Color(245, 245, 245);
    private static final Color TEXT_DARK = new Color(20, 20, 20);

    // Uniformly proportional font hierarchy matching React PDF_Components exactly
    private static final Font FONT_HEADING = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, ISKCON_BLUE);
    private static final Font FONT_SUBHEADING = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 12, ISKCON_BLUE);
    private static final Font FONT_RECEIPT_NO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, TEXT_DARK);
    private static final Font FONT_RECEIPT_NO_TAG = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, ACCENT_PINK);
    private static final Font FONT_DONOR_COPY = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.WHITE);
    private static final Font FONT_DATE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, TEXT_DARK);
    private static final Font FONT_PREACHING_TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, TEXT_DARK);
    private static final Font FONT_PREACHING_TEXT = FontFactory.getFont(FontFactory.HELVETICA, 11, TEXT_DARK);

    private static final Font FONT_AMOUNT_WORDS = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, TEXT_DARK);
    private static final Font FONT_AMOUNT_NUM = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, TEXT_DARK);

    private static final Font FONT_BOX_TAG = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, LABEL_PINK);
    private static final Font FONT_DONOR_LABEL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, TEXT_DARK);
    private static final Font FONT_DONOR_VALUE = FontFactory.getFont(FontFactory.HELVETICA, 14, TEXT_DARK);

    private static final Font FONT_PAYMENT_LABEL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, LABEL_PINK);
    private static final Font FONT_PAYMENT_VALUE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, TEXT_DARK);

    private static final Font FONT_SIGN_LABEL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, LABEL_PINK);
    private static final Font FONT_FOOTER = FontFactory.getFont(FontFactory.HELVETICA, 11, TEXT_DARK);

    private static final Font FONT_PAGE2_HEADING = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, TEXT_DARK);
    private static final Font FONT_PAGE2_BULLET = FontFactory.getFont(FontFactory.HELVETICA, 11.5f, TEXT_DARK);
    private static final Font FONT_PAGE2_MANTRA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13.5f, ISKCON_BLUE);

    /**
     * Generates a PDF receipt for the given donation ID.
     */
    public byte[] generateReceiptPdf(Long donationId) {
        ReceiptDto receiptDto = donationService.getReceipt(donationId);
        return generatePdfFromReceiptDto(receiptDto);
    }

    /**
     * Generates a PDF receipt directly from a ReceiptDto object.
     */
    public byte[] generatePdfFromReceiptDto(ReceiptDto receipt) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 12, 12, 10, 10);
            PdfWriter writer = PdfWriter.getInstance(document, baos);

            // Load background image for Page 1
            Image bgImage = loadClasspathImage("pdf/background.png");
            writer.setPageEvent(new PdfPageEventHelper() {
                @Override
                public void onEndPage(PdfWriter writer, Document doc) {
                    if (writer.getPageNumber() == 1 && bgImage != null) {
                        try {
                            PdfContentByte canvas = writer.getDirectContentUnder();
                            bgImage.scaleAbsolute(doc.getPageSize().getWidth(), doc.getPageSize().getHeight());
                            bgImage.setAbsolutePosition(0, 0);
                            canvas.addImage(bgImage);
                        } catch (Exception e) {
                            log.error("Failed to add background image to receipt PDF", e);
                        }
                    }
                }
            });

            document.open();

            // Page 1: Receipt
            buildPageOne(document, receipt);

            // Page 2: Terms & Conditions
            document.newPage();
            buildPageTwo(document);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generating receipt PDF for donationId: {}", receipt.getDonationId(), e);
            throw new RuntimeException("Error generating PDF receipt: " + e.getMessage(), e);
        }
    }

    private void buildPageOne(Document document, ReceiptDto receipt) throws Exception {
        PdfPTable mainTable = new PdfPTable(1);
        mainTable.setWidthPercentage(100);
        mainTable.setSpacingAfter(0);

        // 1. Top Header Row: Logo + ISKCON Info + Preaching Center & Receipt No
        PdfPTable headerTable = new PdfPTable(new float[]{1.35f, 4.65f});
        headerTable.setWidthPercentage(100);

        // Logo cell
        Image logoImage = loadClasspathImage("pdf/logo.png");
        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        logoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        if (logoImage != null) {
            logoImage.scaleToFit(150, 100);
            logoCell.addElement(logoImage);
        }
        headerTable.addCell(logoCell);

        // Header right cell (Titles + Sub-boxes)
        PdfPCell headerRightCell = new PdfPCell();
        headerRightCell.setBorder(Rectangle.NO_BORDER);

        Paragraph title = new Paragraph("International Society for Krishna Consciousness (ISKCON)", FONT_HEADING);
        title.setAlignment(Element.ALIGN_CENTER);
        headerRightCell.addElement(title);

        Paragraph subtitle = new Paragraph("Founder-Acharya: His Divine Grace A. C. Bhaktivedanta Swami Prabhupada", FONT_SUBHEADING);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(3);
        headerRightCell.addElement(subtitle);

        // Row of 2 boxes: Preaching Centre Box (Left) and Receipt No / Date Box (Right)
        PdfPTable subHeaderTable = new PdfPTable(new float[]{1.2f, 1f});
        subHeaderTable.setWidthPercentage(100);

        // Preaching Centre Box
        PdfPCell centreBoxCell = new PdfPCell();
        centreBoxCell.setBackgroundColor(YELLOW_BG);
        centreBoxCell.setBorderColor(BORDER_RED);
        centreBoxCell.setBorderWidth(1);
        centreBoxCell.setPadding(6);

        Paragraph centreText = new Paragraph();
        centreText.setAlignment(Element.ALIGN_CENTER);
        centreText.add(new Chunk("Preaching Centre\n", FONT_PREACHING_TITLE));
        centreText.add(new Chunk("Doctor's colony, Near Central Hospital (BCCL),\nJagjiwan Nagar, Dhanbad, Jharkhand\nPIN: 826003 | Mobile: 7644070770 / 9903013399\nEmail: acc.iskcondhanbad@gmail.com", FONT_PREACHING_TEXT));
        centreBoxCell.addElement(centreText);
        subHeaderTable.addCell(centreBoxCell);

        // Receipt No & Date Box
        PdfPCell receiptHeaderCell = new PdfPCell();
        receiptHeaderCell.setBorder(Rectangle.NO_BORDER);
        receiptHeaderCell.setPaddingLeft(10);

        PdfPTable receiptMetaTable = new PdfPTable(new float[]{1.1f, 1.9f});
        receiptMetaTable.setWidthPercentage(100);

        // Receipt No row
        PdfPCell rNoLabel = new PdfPCell(new Phrase("Donation\nReceipt No.", FONT_RECEIPT_NO_TAG));
        rNoLabel.setBorder(Rectangle.NO_BORDER);
        rNoLabel.setVerticalAlignment(Element.ALIGN_MIDDLE);
        receiptMetaTable.addCell(rNoLabel);

        PdfPCell rNoVal = new PdfPCell(new Phrase(receipt.getReceiptNumber() != null ? receipt.getReceiptNumber() : "N/A", FONT_RECEIPT_NO));
        rNoVal.setBorder(Rectangle.NO_BORDER);
        rNoVal.setVerticalAlignment(Element.ALIGN_MIDDLE);
        receiptMetaTable.addCell(rNoVal);

        // Donor's copy badge + Date row
        PdfPCell donorCopyCell = new PdfPCell(new Phrase("DONOR'S\nCOPY", FONT_DONOR_COPY));
        donorCopyCell.setBackgroundColor(DONOR_COPY_PINK);
        donorCopyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        donorCopyCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        donorCopyCell.setBorder(Rectangle.NO_BORDER);
        donorCopyCell.setPadding(5);
        receiptMetaTable.addCell(donorCopyCell);

        String formattedDate = formatDateString(receipt.getPaymentDate());
        PdfPCell dateCell = new PdfPCell(new Phrase("Date: " + formattedDate, FONT_DATE));
        dateCell.setBackgroundColor(LIGHT_GRAY_BG);
        dateCell.setBorderColor(Color.LIGHT_GRAY);
        dateCell.setBorderWidth(1);
        dateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        dateCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        dateCell.setPadding(6);
        receiptMetaTable.addCell(dateCell);

        receiptHeaderCell.addElement(receiptMetaTable);
        subHeaderTable.addCell(receiptHeaderCell);

        headerRightCell.addElement(subHeaderTable);
        headerTable.addCell(headerRightCell);

        document.add(headerTable);

        // 2. Amount Input Box (Words + Number)
        PdfPTable amountTable = new PdfPTable(new float[]{3.4f, 1.6f});
        amountTable.setWidthPercentage(100);
        amountTable.setSpacingBefore(7);
        amountTable.setSpacingAfter(7);

        long amountInt = Math.round(receipt.getAmount());
        String words = NumberToWordsUtil.convertToWords(amountInt);
        DecimalFormat df = new DecimalFormat("#,##,##0.00");
        String formattedAmount = "Rs. " + df.format(receipt.getAmount()) + "/-";

        PdfPCell amountWordsCell = new PdfPCell();
        amountWordsCell.setBackgroundColor(LIGHT_GRAY_BG);
        amountWordsCell.setBorderColor(BORDER_RED);
        amountWordsCell.setBorderWidth(1);
        amountWordsCell.setPadding(8);
        amountWordsCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        amountWordsCell.addElement(new Paragraph("Amount in words: " + words, FONT_AMOUNT_WORDS));
        amountTable.addCell(amountWordsCell);

        PdfPCell amountNumCell = new PdfPCell();
        amountNumCell.setBackgroundColor(LIGHT_GRAY_BG);
        amountNumCell.setBorderColor(BORDER_RED);
        amountNumCell.setBorderWidth(1);
        amountNumCell.setPadding(8);
        amountNumCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        amountNumCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        amountNumCell.addElement(new Paragraph(formattedAmount, FONT_AMOUNT_NUM));
        amountTable.addCell(amountNumCell);

        document.add(amountTable);

        // 3. Middle Section: Two Columns (Donor Details on Left, Payment Details + Signature on Right)
        PdfPTable detailsTable = new PdfPTable(new float[]{1f, 1f});
        detailsTable.setWidthPercentage(100);
        detailsTable.setSpacingAfter(7);

        // Left Column: Donor Details Box
        PdfPCell donorBoxCell = new PdfPCell();
        donorBoxCell.setBackgroundColor(LIGHT_GRAY_BG);
        donorBoxCell.setBorderColor(BORDER_RED);
        donorBoxCell.setBorderWidth(1);
        donorBoxCell.setPadding(10);

        Paragraph donorTag = new Paragraph("Donor Details (T&C mentioned backside for 80G/10BE)", FONT_BOX_TAG);
        donorTag.setSpacingAfter(6);
        donorBoxCell.addElement(donorTag);

        donorBoxCell.addElement(createFieldParagraph("Name", receipt.getDonorName()));
        donorBoxCell.addElement(createFieldParagraph("Address", buildFullAddress(receipt)));
        donorBoxCell.addElement(createFieldParagraph("PIN", receipt.getDonorPIN()));
        donorBoxCell.addElement(createFieldParagraph("PAN", receipt.getPan()));
        donorBoxCell.addElement(createFieldParagraph("Mobile", receipt.getMobile()));
        donorBoxCell.addElement(createFieldParagraph("Email", receipt.getEmail()));

        detailsTable.addCell(donorBoxCell);

        // Right Column: Payment Details + Signature Box
        PdfPCell rightColCell = new PdfPCell();
        rightColCell.setBorder(Rectangle.NO_BORDER);
        rightColCell.setPaddingLeft(8);

        PdfPTable rightSubTable = new PdfPTable(1);
        rightSubTable.setWidthPercentage(100);

        // Mode of Payment
        rightSubTable.addCell(createLabeledBox("Mode of Payment (Cheque / Online / UPI / Cash)", receipt.getPaymentMode()));

        // Payment Details with Donation ID
        String paymentDetails = receipt.getTransactionID() != null && !receipt.getTransactionID().isBlank()
                ? receipt.getTransactionID() + " (Donation Id: " + receipt.getDonationId() + ")"
                : "Donation Id: " + receipt.getDonationId();
        rightSubTable.addCell(createLabeledBox("Payment Details (Cheque / Transaction Details)", paymentDetails));

        // Purpose of Donation
        rightSubTable.addCell(createLabeledBox("Purpose of Donation (Corpus / General / Others)", receipt.getPurpose()));

        // Representative Signature Box
        PdfPCell signCell = new PdfPCell();
        signCell.setBackgroundColor(LIGHT_GRAY_BG);
        signCell.setBorderColor(BORDER_RED);
        signCell.setBorderWidth(1);
        signCell.setPadding(5);
        signCell.setMinimumHeight(60);
        signCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        signCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        String signFile = getSignatureFileName(receipt.getDonorCultivatorId());
        Image signImg = loadClasspathImage("pdf/signatures/" + signFile);
        if (signImg != null) {
            signImg.scaleToFit(160, 40);
            signImg.setAlignment(Element.ALIGN_CENTER);
            signCell.addElement(signImg);
        }

        Paragraph signLabel = new Paragraph("Signature of ISKCON Representative", FONT_SIGN_LABEL);
        signLabel.setAlignment(Element.ALIGN_CENTER);
        signCell.addElement(signLabel);

        rightSubTable.addCell(signCell);

        rightColCell.addElement(rightSubTable);
        detailsTable.addCell(rightColCell);

        document.add(detailsTable);

        // 4. Bottom Footer: Registered Office Info
        PdfPTable footerTable = new PdfPTable(1);
        footerTable.setWidthPercentage(100);

        PdfPCell footerCell = new PdfPCell();
        footerCell.setBackgroundColor(YELLOW_BG);
        footerCell.setBorderColor(BORDER_RED);
        footerCell.setBorderWidth(1);
        footerCell.setPadding(6);

        Paragraph regOffice = new Paragraph(
                "Registered Office: Hare Krishna Land, Juhu, Mumbai - 400 049. Mobile: 72088 46210. " +
                        "E-mail: info@iskconindia.org Registered under Maharashtra Public Trust Act 1950, vide Regn. No.: F-2179 (Bom). " +
                        "Unique Regn. No. (80G): AAAT10017PF20219",
                FONT_FOOTER
        );
        regOffice.setAlignment(Element.ALIGN_CENTER);
        footerCell.addElement(regOffice);
        footerTable.addCell(footerCell);

        document.add(footerTable);
    }

    private void buildPageTwo(Document document) throws Exception {
        Paragraph heading = new Paragraph("Please note Terms and Conditions (T&C):", FONT_PAGE2_HEADING);
        heading.setSpacingAfter(12);
        document.add(heading);

        String[] terms = new String[]{
                "This donation receipt is an acknowledgement only and not for the purpose of claiming 80G deduction.",
                "Form No. 10BE, i.e., Certificate of donation under clause (ix) of sub-section (5) of section 80G of the Income Tax Act, 1961, will be issued to you as per provisions of Income-tax Act, 1961, and rules made thereunder. Generally 10BE will be issued by 31st May of the following financial year.",
                "For all type of donations, irrespective of amount and mode of payment, full legal name and address with PIN are required. Further PAN is compulsory to obtain Form No. 10BE. Please ensure that the same are mentioned correctly in the donation receipt.",
                "Form No. 10BE is not available for any cash donation.",
                "10BE will be available in PDF version only. Please ensure to mention correct WhatsApp number and E-mail id to receive the same.",
                "PAN is compulsory for all donation of Rs. 50,000/- or more.",
                "In case of payment by cheque, this donation receipt is valid subject to clearance of the cheque.",
                "ISKCON's Unique Registration Number for 80G-AAAT10017PF20219 is valid till March 31, 2026 and to be renewed thereafter periodically as per provisions of Income-tax Act, 1961, and rules made thereunder.",
                "In case of any error/discrepancy in this receipt, including your Name, address and PAN, E-mail ID, WhatsApp number etc., please contact the receipt issuing centre for correction."
        };

        com.lowagie.text.List list = new com.lowagie.text.List(false, 18);
        list.setListSymbol(new Chunk("\u2022 ", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, TEXT_DARK)));
        for (String term : terms) {
            ListItem item = new ListItem(term, FONT_PAGE2_BULLET);
            item.setSpacingAfter(6f);
            list.add(item);
        }
        document.add(list);

        Paragraph thankYou = new Paragraph("\nThank you for your support.",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, TEXT_DARK));
        thankYou.setAlignment(Element.ALIGN_CENTER);
        thankYou.setSpacingBefore(10);
        document.add(thankYou);

        Paragraph chant = new Paragraph("Please chant",
                FontFactory.getFont(FontFactory.HELVETICA, 12, TEXT_DARK));
        chant.setAlignment(Element.ALIGN_CENTER);
        chant.setSpacingBefore(4);
        document.add(chant);

        Paragraph mantra = new Paragraph(
                "HARE KRISHNA HARE KRISHNA KRISHNA KRISHNA HARE HARE\n" +
                        "HARE RAMA HARE RAMA RAMA RAMA HARE HARE",
                FONT_PAGE2_MANTRA
        );
        mantra.setAlignment(Element.ALIGN_CENTER);
        mantra.setSpacingBefore(6);
        mantra.setSpacingAfter(6);
        document.add(mantra);

        Paragraph happy = new Paragraph("and be happy.",
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 12, TEXT_DARK));
        happy.setAlignment(Element.ALIGN_CENTER);
        document.add(happy);
    }

    private PdfPCell createLabeledBox(String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(LIGHT_GRAY_BG);
        cell.setBorderColor(BORDER_RED);
        cell.setBorderWidth(1);
        cell.setPadding(3);
        cell.setPaddingBottom(5);

        Paragraph pLabel = new Paragraph(label, FONT_PAYMENT_LABEL);
        pLabel.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(pLabel);

        Paragraph pVal = new Paragraph(value != null && !value.isBlank() ? value : " ", FONT_PAYMENT_VALUE);
        pVal.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(pVal);

        return cell;
    }

    private Paragraph createFieldParagraph(String label, String value) {
        Paragraph p = new Paragraph();
        p.setSpacingAfter(4f);
        p.add(new Chunk(label + ": ", FONT_DONOR_LABEL));
        p.add(new Chunk(value != null && !value.isBlank() ? value : "", FONT_DONOR_VALUE));
        return p;
    }

    private String buildFullAddress(ReceiptDto receipt) {
        StringBuilder sb = new StringBuilder();
        if (receipt.getDonorAddress() != null && !receipt.getDonorAddress().isBlank()) {
            sb.append(receipt.getDonorAddress().trim());
        }
        String city = receipt.getCity();
        String state = receipt.getState();
        if (city != null && !city.isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(city.trim());
        }
        if (state != null && !state.isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(state.trim());
        }
        return sb.toString();
    }

    private String getSignatureFileName(String donorCultivatorId) {
        if (donorCultivatorId == null) {
            return "bhim_sign.png";
        }
        return switch (donorCultivatorId.trim()) {
            case "1" -> "sunita_sign.png";
            case "2" -> "bhim_sign.png";
            case "3" -> "rajeev_sign.png";
            case "4" -> "spap_sign.png";
            case "5" -> "dgp_sign.png";
            case "6" -> "nibedita_sign.png";
            case "7" -> "suman_sign.png";
            case "8", "9", "420" -> "npp_sign.png";
            default -> "bhim_sign.png";
        };
    }

    private Image loadClasspathImage(String path) {
        try {
            ClassPathResource res = new ClassPathResource(path);
            if (res.exists()) {
                try (InputStream is = res.getInputStream()) {
                    byte[] bytes = is.readAllBytes();
                    return Image.getInstance(bytes);
                }
            }
        } catch (Exception e) {
            log.warn("Could not load image resource at {}: {}", path, e.getMessage());
        }
        return null;
    }

    private String formatDateString(String dateString) {
        if (dateString == null || dateString.isBlank()) {
            return "";
        }
        try {
            // Check if already in yyyy-MM-dd or timestamp format
            if (dateString.contains("T")) {
                dateString = dateString.substring(0, dateString.indexOf("T"));
            }
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = inputFormat.parse(dateString);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM, yyyy");
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateString;
        }
    }
}

