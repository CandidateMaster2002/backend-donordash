package com.iskcondhanbad.donordash.service;

import com.iskcondhanbad.donordash.dto.ReceiptDto;
import com.iskcondhanbad.donordash.utils.NumberToWordsUtil;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
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
import java.util.Locale;

@Slf4j
@Service
public class PdfReceiptService {

    @Autowired
    private DonationService donationService;

    // Palette matching frontend styling exactly
    private static final Color ISKCON_BLUE = new Color(2, 62, 125);       // #023e7d
    private static final Color ACCENT_PINK = new Color(255, 54, 171);     // #ff36ab
    private static final Color DONOR_COPY_PINK = new Color(255, 135, 171);// #ff87ab
    private static final Color LABEL_PINK = new Color(255, 10, 84);       // #ff0a54
    private static final Color YELLOW_BG = new Color(255, 229, 102);      // #ffe566
    private static final Color BORDER_RED = new Color(208, 0, 0);         // #d00000
    private static final Color LIGHT_GRAY_BG = new Color(238, 238, 238);  // #eeeeee
    private static final Color TEXT_DARK = new Color(0, 0, 0);            // #000000

    private static BaseFont BF_HELVETICA_BOLD;
    private static BaseFont BF_HELVETICA;
    static {
        try {
            BF_HELVETICA_BOLD = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            BF_HELVETICA = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
        } catch (Exception e) {
            log.error("Failed to initialize BaseFonts", e);
        }
    }

    // Font hierarchy
    private static final Font FONT_HEADING = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22.5f, ISKCON_BLUE);
    private static final Font FONT_SUBHEADING = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 11.5f, ISKCON_BLUE);

    private static final Font FONT_RECEIPT_NO_TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 17.5f, ACCENT_PINK);
    private static final Font FONT_RECEIPT_NO_SUB = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13.5f, ACCENT_PINK);
    private static final Font FONT_RECEIPT_NO_VAL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20f, TEXT_DARK);
    private static final Font FONT_DONOR_COPY = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16.5f, Color.WHITE);
    private static final Font FONT_DATE_VAL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 17.5f, TEXT_DARK);

    private static final Font FONT_PREACHING_TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11.5f, TEXT_DARK);
    private static final Font FONT_PREACHING_TEXT = FontFactory.getFont(FontFactory.HELVETICA, 10.5f, TEXT_DARK);

    private static final Font FONT_AMOUNT_WORDS = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 17.5f, TEXT_DARK);
    private static final Font FONT_AMOUNT_NUM = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 19.5f, TEXT_DARK);

    private static final Font FONT_DONOR_LABEL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14.5f, TEXT_DARK);
    private static final Font FONT_DONOR_VALUE = FontFactory.getFont(FontFactory.HELVETICA, 14.5f, TEXT_DARK);

    private static final Font FONT_PAYMENT_VALUE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16.5f, TEXT_DARK);
    private static final Font FONT_FOOTER = FontFactory.getFont(FontFactory.HELVETICA, 11.5f, TEXT_DARK);

    // Page 2 fonts
    private static final Font FONT_PAGE2_HEADING = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16.5f, TEXT_DARK);
    private static final Font FONT_PAGE2_BULLET = FontFactory.getFont(FontFactory.HELVETICA, 13f, TEXT_DARK);
    private static final Font FONT_PAGE2_MANTRA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16.5f, ISKCON_BLUE);

    /**
     * Cell event that renders a rounded rectangular box with a border-cutting legend tag
     * matching the frontend fieldset/legend styling.
     */
    public static class RoundedLegendBoxEvent implements PdfPCellEvent {
        private final String legendText;
        private final Color tagColor;
        private final Color borderColor;
        private final Color bgColor;
        private final float cornerRadius;
        private final boolean isBottomTag;
        private final float tagOffsetPercent; // < 0 for centered, 0.0-1.0 for offset from left
        private final float tagFontSize;
        private final BaseFont baseFont;

        public RoundedLegendBoxEvent(String legendText, Color tagColor, Color borderColor, Color bgColor,
                                     float cornerRadius, boolean isBottomTag, float tagOffsetPercent,
                                     float tagFontSize, BaseFont baseFont) {
            this.legendText = legendText;
            this.tagColor = tagColor;
            this.borderColor = borderColor;
            this.bgColor = bgColor;
            this.cornerRadius = cornerRadius;
            this.isBottomTag = isBottomTag;
            this.tagOffsetPercent = tagOffsetPercent;
            this.tagFontSize = tagFontSize;
            this.baseFont = baseFont;
        }

        @Override
        public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
            float x = position.getLeft();
            float y = position.getBottom();
            float w = position.getWidth();
            float h = position.getHeight();

            PdfContentByte bgCanvas = canvases[PdfPTable.BACKGROUNDCANVAS];
            bgCanvas.saveState();
            bgCanvas.setColorFill(bgColor);
            bgCanvas.setColorStroke(borderColor);
            bgCanvas.setLineWidth(1.0f);
            bgCanvas.roundRectangle(x, y, w, h, cornerRadius);
            bgCanvas.fillStroke();
            bgCanvas.restoreState();

            if (legendText != null && !legendText.isBlank() && baseFont != null) {
                float textWidth = baseFont.getWidthPoint(legendText, tagFontSize);
                float padX = 8.0f;
                float padY = 2.5f;
                float tagW = textWidth + (2 * padX);
                float tagH = tagFontSize + (2 * padY);

                float tagX;
                if (tagOffsetPercent < 0) {
                    tagX = x + (w - tagW) / 2.0f;
                } else {
                    tagX = x + (w * tagOffsetPercent);
                    if (tagX < x + cornerRadius + 2) {
                        tagX = x + cornerRadius + 2;
                    }
                    if (tagX + tagW > x + w - cornerRadius - 2) {
                        tagX = x + w - cornerRadius - 2 - tagW;
                    }
                }

                float borderY = isBottomTag ? y : (y + h);
                float tagY = borderY - (tagH / 2.0f);

                PdfContentByte textCanvas = canvases[PdfPTable.TEXTCANVAS];
                textCanvas.saveState();

                // Erase the border line by filling tag background
                textCanvas.setColorFill(bgColor);
                textCanvas.roundRectangle(tagX, tagY, tagW, tagH, 3.0f);
                textCanvas.fill();

                // Draw tag text
                textCanvas.beginText();
                textCanvas.setFontAndSize(baseFont, tagFontSize);
                textCanvas.setColorFill(tagColor);
                float textBaseline = tagY + padY + 1.2f;
                textCanvas.showTextAligned(Element.ALIGN_CENTER, legendText, tagX + (tagW / 2.0f), textBaseline, 0);
                textCanvas.endText();
                textCanvas.restoreState();
            }
        }
    }

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
            Document document = new Document(PageSize.A4.rotate(), 16, 16, 12, 12);
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
        // 1. Top Most Header: Center aligned, spanning full width
        Paragraph title = new Paragraph("International Society for Krishna Consciousness (ISKCON)", FONT_HEADING);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingBefore(0);
        title.setSpacingAfter(2);
        document.add(title);

        Paragraph subtitle = new Paragraph("Founder-Acharya: His Divine Grace A. C. Bhaktivedanta Swami Prabhupada", FONT_SUBHEADING);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(12); // Space between header and lower section
        document.add(subtitle);

        // 2. Lower Section: Logo, Address box, and Receipt section all in the SAME ROW LEVEL
        PdfPTable topRowTable = new PdfPTable(new float[]{1.15f, 2.30f, 2.55f});
        topRowTable.setWidthPercentage(100);

        // Col 1: Logo (Left - enlarged)
        Image logoImage = loadClasspathImage("pdf/logo.png");
        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        logoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        if (logoImage != null) {
            logoImage.scaleToFit(165, 110);
            logoCell.addElement(logoImage);
        }
        topRowTable.addCell(logoCell);

        // Col 2: Preaching Centre Address Box (Center)
        PdfPCell centreBoxCell = new PdfPCell();
        centreBoxCell.setBackgroundColor(YELLOW_BG);
        centreBoxCell.setBorderColor(BORDER_RED);
        centreBoxCell.setBorderWidth(1);
        centreBoxCell.setPadding(4);
        centreBoxCell.setPaddingLeft(6);
        centreBoxCell.setPaddingRight(6);
        centreBoxCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        Paragraph centreText = new Paragraph();
        centreText.setAlignment(Element.ALIGN_CENTER);
        centreText.setLeading(12.5f);
        centreText.add(new Chunk("Preaching Centre\n", FONT_PREACHING_TITLE));
        centreText.add(new Chunk("Doctor's colony, Near Central Hospital (BCCL),\nJagjiwan Nagar, Dhanbad, Jharkhand\nPIN: 826003\nMobile No: 7644070770/9903013399\nEmail: acc.iskcondhanbad@gmail.com", FONT_PREACHING_TEXT));
        centreBoxCell.addElement(centreText);
        topRowTable.addCell(centreBoxCell);

        // Col 3: Receipt No & Date Section (Right)
        PdfPCell receiptCell = new PdfPCell();
        receiptCell.setBorder(Rectangle.NO_BORDER);
        receiptCell.setPaddingLeft(10);
        receiptCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        // Sub-table Row 1: Receipt Number (Single line)
        PdfPTable rNoTable = new PdfPTable(new float[]{0.95f, 2.05f});
        rNoTable.setWidthPercentage(100);

        PdfPCell rNoLabel = new PdfPCell();
        rNoLabel.setBorder(Rectangle.NO_BORDER);
        rNoLabel.setVerticalAlignment(Element.ALIGN_MIDDLE);
        Paragraph rNoPara = new Paragraph();
        rNoPara.add(new Chunk("Donation\n", FONT_RECEIPT_NO_TITLE));
        rNoPara.add(new Chunk("Receipt No.", FONT_RECEIPT_NO_SUB));
        rNoLabel.addElement(rNoPara);
        rNoTable.addCell(rNoLabel);

        PdfPCell rNoVal = new PdfPCell();
        rNoVal.setBorder(Rectangle.NO_BORDER);
        rNoVal.setVerticalAlignment(Element.ALIGN_MIDDLE);
        rNoVal.setPaddingLeft(10);
        Paragraph rNoValPara = new Paragraph(receipt.getReceiptNumber() != null ? receipt.getReceiptNumber() : "N/A", FONT_RECEIPT_NO_VAL);
        rNoVal.addElement(rNoValPara);
        rNoTable.addCell(rNoVal);

        receiptCell.addElement(rNoTable);

        // Sub-table Row 2: DONOR'S COPY + Date box with "Date" legend tag
        PdfPTable donorDateTable = new PdfPTable(new float[]{0.95f, 2.05f});
        donorDateTable.setWidthPercentage(100);
        donorDateTable.setSpacingBefore(7);

        PdfPCell donorCopyCell = new PdfPCell();
        donorCopyCell.setBackgroundColor(DONOR_COPY_PINK);
        donorCopyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        donorCopyCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        donorCopyCell.setBorder(Rectangle.NO_BORDER);
        donorCopyCell.setPadding(3);
        donorCopyCell.setPaddingTop(5);
        donorCopyCell.setPaddingBottom(5);
        donorCopyCell.setMinimumHeight(36f);
        Paragraph donorCopyPara = new Paragraph();
        donorCopyPara.setAlignment(Element.ALIGN_CENTER);
        donorCopyPara.setLeading(14f);
        donorCopyPara.add(new Chunk("DONOR'S\nCOPY", FONT_DONOR_COPY));
        donorCopyCell.addElement(donorCopyPara);
        donorDateTable.addCell(donorCopyCell);

        String formattedDate = formatDateString(receipt.getPaymentDate());
        PdfPCell dateCell = new PdfPCell();
        dateCell.setBorder(Rectangle.NO_BORDER);
        dateCell.setPadding(4);
        dateCell.setPaddingTop(7);
        dateCell.setPaddingBottom(4);
        dateCell.setMinimumHeight(36f);
        dateCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        dateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        dateCell.setCellEvent(new RoundedLegendBoxEvent(
                "Date", LABEL_PINK, Color.RED, LIGHT_GRAY_BG,
                4f, false, -1, 12.5f, BF_HELVETICA_BOLD
        ));
        Paragraph datePara = new Paragraph(formattedDate, FONT_DATE_VAL);
        datePara.setAlignment(Element.ALIGN_CENTER);
        dateCell.addElement(datePara);
        donorDateTable.addCell(dateCell);

        receiptCell.addElement(donorDateTable);
        topRowTable.addCell(receiptCell);

        document.add(topRowTable);

        // 3. Amount Input Box: Significant space between Address/Receipt and Amount Box
        PdfPTable amountOuterTable = new PdfPTable(1);
        amountOuterTable.setWidthPercentage(100);
        amountOuterTable.setSpacingBefore(20); // Significant space between address/receipt and amount box
        amountOuterTable.setSpacingAfter(20);  // Proper space before Donor Details while keeping alignment

        PdfPCell amountOuterCell = new PdfPCell();
        amountOuterCell.setBorder(Rectangle.NO_BORDER);
        amountOuterCell.setPadding(9);
        amountOuterCell.setPaddingTop(13);
        amountOuterCell.setPaddingBottom(9);
        amountOuterCell.setCellEvent(new RoundedLegendBoxEvent(
                "Donation Amount in Rupees", LABEL_PINK, Color.RED, LIGHT_GRAY_BG,
                4f, false, -1, 13f, BF_HELVETICA_BOLD
        ));

        PdfPTable amountInnerTable = new PdfPTable(new float[]{3.4f, 1.6f});
        amountInnerTable.setWidthPercentage(100);

        long amountInt = Math.round(receipt.getAmount());
        String words = NumberToWordsUtil.convertToWords(amountInt);

        // Words cell (left)
        PdfPCell wordsCell = new PdfPCell();
        wordsCell.setBorder(Rectangle.NO_BORDER);
        wordsCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        wordsCell.setPaddingLeft(6);
        wordsCell.addElement(new Paragraph(words, FONT_AMOUNT_WORDS));
        amountInnerTable.addCell(wordsCell);

        // Amount number cell: Rupee icon badge + amount number inline with subtle space
        DecimalFormat df = (receipt.getAmount() % 1 == 0)
                ? new DecimalFormat("#,##,##0")
                : new DecimalFormat("#,##,##0.00");
        String formattedAmountNum = df.format(receipt.getAmount());

        PdfPCell amountValCell = new PdfPCell();
        amountValCell.setBorder(Rectangle.NO_BORDER);
        amountValCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        amountValCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        amountValCell.setPaddingRight(24);

        Paragraph amountPara = new Paragraph();
        amountPara.setAlignment(Element.ALIGN_RIGHT);

        Image rupeeIcon = loadClasspathImage("pdf/rupee_icon.png");
        if (rupeeIcon != null) {
            rupeeIcon.scaleToFit(22, 22);
            // Offset vertically by -4.5f so the 22pt icon center aligns with 19.5pt font cap-height
            Chunk iconChunk = new Chunk(rupeeIcon, 0, -4.5f, false);
            amountPara.add(iconChunk);
            // Small subtle spacing between rupee icon and total amount
            amountPara.add(new Chunk(" ", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        }
        amountPara.add(new Chunk(formattedAmountNum, FONT_AMOUNT_NUM));

        amountValCell.addElement(amountPara);
        amountInnerTable.addCell(amountValCell);

        amountOuterCell.addElement(amountInnerTable);
        amountOuterTable.addCell(amountOuterCell);
        document.add(amountOuterTable);

        // 4. Middle Section: Two Columns (Donor Details on Left, Payment Details + Signature on Right)
        PdfPTable detailsTable = new PdfPTable(new float[]{1f, 1f});
        detailsTable.setWidthPercentage(100);
        detailsTable.setSpacingAfter(22); // Space between details section and bottommost footer

        // Left Column: Donor Details Box
        PdfPCell donorBoxCell = new PdfPCell();
        donorBoxCell.setBorder(Rectangle.NO_BORDER);
        donorBoxCell.setPadding(12);
        donorBoxCell.setPaddingTop(16);
        donorBoxCell.setCellEvent(new RoundedLegendBoxEvent(
                "Donor Details (T&C mentioned backside for 80G/10BE)", LABEL_PINK, Color.RED, LIGHT_GRAY_BG,
                4f, false, -1, 13f, BF_HELVETICA_BOLD
        ));

        donorBoxCell.addElement(createFieldParagraph("Name", receipt.getDonorName()));
        donorBoxCell.addElement(createFieldParagraph("Address", buildFullAddress(receipt)));

        // Spacer between address and PIN
        Paragraph spacer = new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 8));
        donorBoxCell.addElement(spacer);

        donorBoxCell.addElement(createFieldParagraph("PIN", receipt.getDonorPIN()));
        donorBoxCell.addElement(createFieldParagraph("PAN", receipt.getPan()));
        donorBoxCell.addElement(createFieldParagraph("Mobile", receipt.getMobile()));
        donorBoxCell.addElement(createFieldParagraph("Email", receipt.getEmail()));

        detailsTable.addCell(donorBoxCell);

        // Right Column: Payment Details + Signature Box
        PdfPCell rightColCell = new PdfPCell();
        rightColCell.setBorder(Rectangle.NO_BORDER);
        rightColCell.setPaddingLeft(10);

        PdfPTable rightSubTable = new PdfPTable(1);
        rightSubTable.setWidthPercentage(100);

        // Mode of Payment
        rightSubTable.addCell(createRoundedFieldBox(
                "Mode of Payment (Cheque / Online / UPI / Cash)",
                receipt.getPaymentMode(),
                41f
        ));

        rightSubTable.addCell(createSpacingCell(11f));

        // Payment Details with Donation ID
        String paymentDetails = receipt.getTransactionID() != null && !receipt.getTransactionID().isBlank()
                ? receipt.getTransactionID() + " (Donation Id: " + receipt.getDonationId() + ")"
                : "Donation Id: " + receipt.getDonationId();
        rightSubTable.addCell(createRoundedFieldBox(
                "Payment Details (Cheque / Transaction Details)",
                paymentDetails,
                41f
        ));

        rightSubTable.addCell(createSpacingCell(11f));

        // Purpose of Donation
        rightSubTable.addCell(createRoundedFieldBox(
                "Purpose of Donation (Corpus / General / Others)",
                receipt.getPurpose(),
                41f
        ));

        rightSubTable.addCell(createSpacingCell(11f));

        // Representative Signature Box (Bottom legend tag in ISKCON blue)
        PdfPCell signCell = new PdfPCell();
        signCell.setBorder(Rectangle.NO_BORDER);
        signCell.setPadding(3);
        signCell.setPaddingBottom(8);
        signCell.setMinimumHeight(64f);
        signCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        signCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        signCell.setCellEvent(new RoundedLegendBoxEvent(
                "Signature of ISKCON Representative", ISKCON_BLUE, Color.RED, LIGHT_GRAY_BG,
                4f, true, 0.65f, 10.5f, BF_HELVETICA_BOLD
        ));

        String signFile = getSignatureFileName(receipt.getDonorCultivatorId());
        Image signImg = loadClasspathImage("pdf/signatures/" + signFile);
        if (signImg != null) {
            signImg.scaleToFit(160, 44);
            signImg.setAlignment(Element.ALIGN_CENTER);
            signCell.addElement(signImg);
        }

        rightSubTable.addCell(signCell);

        rightColCell.addElement(rightSubTable);
        detailsTable.addCell(rightColCell);

        document.add(detailsTable);

        // 5. Bottom Footer: Registered Office Info (Fills down to the bottom)
        PdfPTable footerTable = new PdfPTable(1);
        footerTable.setWidthPercentage(100);

        PdfPCell footerCell = new PdfPCell();
        footerCell.setBackgroundColor(YELLOW_BG);
        footerCell.setBorderColor(BORDER_RED);
        footerCell.setBorderWidth(1);
        footerCell.setPadding(10);

        Paragraph regOffice = new Paragraph(
                "Registered Office: Hare Krishna Land, Juhu, Mumbai - 400 049. Mobile: 72088 46210. " +
                        "E-mail: info@iskconindia.org Registered under Maharashtra Public Trust Act 1950, vide Regn. No.: F-2179 (Bom). " +
                        "Unique Regn. No. (80G): AAAT10017PF20219",
                FONT_FOOTER
        );
        regOffice.setAlignment(Element.ALIGN_CENTER);
        regOffice.setLeading(15f);
        footerCell.addElement(regOffice);
        footerTable.addCell(footerCell);

        document.add(footerTable);
    }

    private void buildPageTwo(Document document) throws Exception {
        Paragraph heading = new Paragraph("Please note Terms and Conditions (T&C):", FONT_PAGE2_HEADING);
        heading.setSpacingBefore(2);
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

        com.lowagie.text.List list = new com.lowagie.text.List(false, 20);
        list.setListSymbol(new Chunk("\u2022 ", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13.5f, TEXT_DARK)));
        for (String term : terms) {
            ListItem item = new ListItem(term, FONT_PAGE2_BULLET);
            item.setSpacingAfter(7.5f);
            item.setLeading(18f);
            list.add(item);
        }
        document.add(list);

        Paragraph thankYou = new Paragraph("\nThank you for your support.",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16f, TEXT_DARK));
        thankYou.setAlignment(Element.ALIGN_CENTER);
        thankYou.setSpacingBefore(14);
        document.add(thankYou);

        Paragraph chant = new Paragraph("Please chant",
                FontFactory.getFont(FontFactory.HELVETICA, 13.5f, TEXT_DARK));
        chant.setAlignment(Element.ALIGN_CENTER);
        chant.setSpacingBefore(6);
        document.add(chant);

        Paragraph mantra = new Paragraph(
                "HARE KRISHNA HARE KRISHNA KRISHNA KRISHNA HARE HARE\n" +
                        "HARE RAMA HARE RAMA RAMA RAMA HARE HARE",
                FONT_PAGE2_MANTRA
        );
        mantra.setAlignment(Element.ALIGN_CENTER);
        mantra.setSpacingBefore(8);
        mantra.setSpacingAfter(8);
        document.add(mantra);

        Paragraph happy = new Paragraph("and be happy.",
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 13.5f, TEXT_DARK));
        happy.setAlignment(Element.ALIGN_CENTER);
        happy.setSpacingBefore(4);
        document.add(happy);
    }

    private PdfPCell createRoundedFieldBox(String label, String value, float minHeight) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(3);
        cell.setPaddingTop(7);
        cell.setPaddingBottom(3);
        cell.setMinimumHeight(minHeight);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setCellEvent(new RoundedLegendBoxEvent(
                label, LABEL_PINK, Color.RED, LIGHT_GRAY_BG,
                4f, false, -1, 12f, BF_HELVETICA_BOLD
        ));

        Paragraph pVal = new Paragraph(value != null && !value.isBlank() ? value : " ", FONT_PAYMENT_VALUE);
        pVal.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(pVal);

        return cell;
    }

    private PdfPCell createSpacingCell(float height) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setFixedHeight(height);
        return cell;
    }

    private Paragraph createFieldParagraph(String label, String value) {
        Paragraph p = new Paragraph();
        p.setSpacingAfter(5.5f);
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
            if (dateString.contains("T")) {
                dateString = dateString.substring(0, dateString.indexOf("T"));
            }
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = inputFormat.parse(dateString);
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateString;
        }
    }
}
