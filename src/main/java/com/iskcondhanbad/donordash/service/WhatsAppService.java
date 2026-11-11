package com.iskcondhanbad.donordash.service;

import com.iskcondhanbad.donordash.dto.ReceiptDto;
import com.iskcondhanbad.donordash.dto.WhatsAppSendResponseDto;
import com.iskcondhanbad.donordash.model.StoredDonation;
import com.iskcondhanbad.donordash.model.StoredDonor;
import com.iskcondhanbad.donordash.repository.DonationRepository;
import com.iskcondhanbad.donordash.repository.DonorRepository;
import com.iskcondhanbad.donordash.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class WhatsAppService {

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private DonorRepository donorRepository;

    @Autowired
    private DonationService donationService;

    @Autowired
    private PdfReceiptService pdfReceiptService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${whatsapp.api.version:v21.0}")
    private String apiVersion;

    @Value("${whatsapp.phone-number-id:}")
    private String phoneNumberId;

    @Value("${whatsapp.access-token:}")
    private String accessToken;

    @Value("${whatsapp.template.name:donation_receipt}")
    private String templateName;

    @Value("${whatsapp.template.language:en}")
    private String templateLanguage;

    @Value("${whatsapp.template.param.name:name}")
    private String paramName;

    @Value("${whatsapp.template.param.address:address}")
    private String paramAddress;

    @Value("${whatsapp.template.param.pan:pan}")
    private String paramPan;

    @Value("${whatsapp.template.param.amount:amount}")
    private String paramAmount;

    /**
     * Sends a WhatsApp message with the receipt PDF attachment to the donor for the given donationId.
     *
     * @param donationId The ID of the donation
     * @return WhatsAppSendResponseDto with details of the sent message
     */
    public WhatsAppSendResponseDto sendDonationReceipt(Long donationId) {
        if (donationId == null) {
            throw new IllegalArgumentException("Donation ID must not be null");
        }

        StoredDonation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new IllegalArgumentException("Donation not found with ID: " + donationId));

        if (!Constants.VERIFIED.equalsIgnoreCase(donation.getStatus())) {
            throw new IllegalArgumentException("Cannot send receipt for unverified donation. Current status: " + donation.getStatus());
        }

        StoredDonor donor = donorRepository.findById(donation.getDonor().getId())
                .orElseThrow(() -> new IllegalArgumentException("Donor not found for donation ID: " + donationId));

        String rawMobile = donor.getMobileNumber();
        if (rawMobile == null || rawMobile.isBlank()) {
            throw new IllegalArgumentException("Donor does not have a mobile number configured (donor ID: " + donor.getId() + ")");
        }

        String recipientMobile = normalizePhoneNumber(rawMobile);
        if (recipientMobile.isBlank()) {
            throw new IllegalArgumentException("Invalid mobile number format for donor: " + rawMobile);
        }

        ReceiptDto receipt = donationService.getReceipt(donationId);
        if (receipt == null) {
            throw new IllegalStateException("Unable to retrieve receipt data for donation ID: " + donationId);
        }

        // Generate the PDF receipt bytes
        byte[] pdfBytes = pdfReceiptService.generateReceiptPdf(donationId);
        if (pdfBytes == null || pdfBytes.length == 0) {
            throw new IllegalStateException("Generated receipt PDF is empty for donation ID: " + donationId);
        }

        String receiptNumber = receipt.getReceiptNumber() != null ? receipt.getReceiptNumber() : String.valueOf(donationId);
        String pdfFilename = "Receipt_" + receiptNumber + ".pdf";

        // Step 1: Upload the PDF document to Meta WhatsApp Cloud Media API
        log.info("Uploading receipt PDF to WhatsApp Media API for donationId: {}", donationId);
        String mediaId = uploadMedia(pdfBytes, pdfFilename, "application/pdf");
        log.info("Receipt PDF uploaded successfully. Media ID: {}", mediaId);

        // Step 2: Prepare template parameters (Name, Address, PAN, Amount)
        String donorName = sanitize(receipt.getDonorName() != null ? receipt.getDonorName() : donor.getName(), "Donor");
        String donorAddress = sanitize(receipt.getDonorAddress() != null ? receipt.getDonorAddress() : donor.getAddress(), "N/A");
        String pan = sanitize(receipt.getPan() != null ? receipt.getPan() : donor.getPanNumber(), "N/A");
        Double amountVal = receipt.getAmount() > 0 ? receipt.getAmount() : (donation.getAmount() != null ? donation.getAmount() : 0.0);
        String formattedAmount = formatAmount(amountVal);

        // Step 3: Send the WhatsApp Template Message
        log.info("Sending WhatsApp template '{}' to recipient: {}", templateName, recipientMobile);
        Map<String, Object> apiResponse = sendTemplateMessage(
                recipientMobile,
                mediaId,
                pdfFilename,
                donorName,
                donorAddress,
                pan,
                formattedAmount
        );

        String messageId = extractMessageId(apiResponse);

        log.info("WhatsApp receipt sent successfully for donationId: {}, messageId: {}", donationId, messageId);

        return WhatsAppSendResponseDto.builder()
                .success(true)
                .message("WhatsApp receipt sent successfully")
                .messageId(messageId)
                .recipientMobile(recipientMobile)
                .mediaId(mediaId)
                .receiptId(receiptNumber)
                .amount(amountVal)
                .donorName(donorName)
                .build();
    }

    /**
     * Uploads media bytes to Meta WhatsApp Cloud Media endpoint.
     */
    public String uploadMedia(byte[] fileBytes, String filename, String mimeType) {
        validateCredentials();

        String url = String.format("https://graph.facebook.com/%s/%s/media", apiVersion, phoneNumberId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("messaging_product", "whatsapp");
        body.add("type", mimeType);

        ByteArrayResource resource = new ByteArrayResource(fileBytes) {
            @Override
            public String getFilename() {
                return filename;
            }
        };

        HttpHeaders fileHeaders = new HttpHeaders();
        fileHeaders.setContentType(MediaType.parseMediaType(mimeType));
        body.add("file", new HttpEntity<>(resource, fileHeaders));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object id = response.getBody().get("id");
                if (id != null) {
                    return id.toString();
                }
            }
            throw new RuntimeException("Unexpected response from WhatsApp Media API: " + response.getBody());
        } catch (HttpStatusCodeException ex) {
            log.error("WhatsApp Media upload failed with status {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new RuntimeException("WhatsApp Media API error (" + ex.getStatusCode() + "): " + ex.getResponseBodyAsString(), ex);
        }
    }

    /**
     * Sends a template message with document header and named body text parameters.
     */
    public Map<String, Object> sendTemplateMessage(String to,
                                                   String mediaId,
                                                   String pdfFilename,
                                                   String donorName,
                                                   String donorAddress,
                                                   String pan,
                                                   String formattedAmount) {
        validateCredentials();

        String url = String.format("https://graph.facebook.com/%s/%s/messages", apiVersion, phoneNumberId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("messaging_product", "whatsapp");
        payload.put("recipient_type", "individual");
        payload.put("to", normalizePhoneNumber(to));
        payload.put("type", "template");

        Map<String, Object> template = new LinkedHashMap<>();
        template.put("name", templateName);

        Map<String, Object> language = new LinkedHashMap<>();
        language.put("code", templateLanguage);
        template.put("language", language);

        List<Map<String, Object>> components = new ArrayList<>();

        // Document header component
        if (mediaId != null && !mediaId.isBlank()) {
            Map<String, Object> headerComponent = new LinkedHashMap<>();
            headerComponent.put("type", "header");

            Map<String, Object> docParam = new LinkedHashMap<>();
            docParam.put("type", "document");

            Map<String, Object> doc = new LinkedHashMap<>();
            doc.put("id", mediaId);
            doc.put("filename", pdfFilename != null ? pdfFilename : "receipt.pdf");
            docParam.put("document", doc);

            headerComponent.put("parameters", List.of(docParam));
            components.add(headerComponent);
        }

        // Body component: parameters with parameter_name for named parameter templates
        Map<String, Object> bodyComponent = new LinkedHashMap<>();
        bodyComponent.put("type", "body");

        List<Map<String, String>> bodyParams = new ArrayList<>();
        bodyParams.add(createTextParam(paramName, donorName));
        bodyParams.add(createTextParam(paramAddress, donorAddress));
        bodyParams.add(createTextParam(paramPan, pan));
        bodyParams.add(createTextParam(paramAmount, formattedAmount));

        bodyComponent.put("parameters", bodyParams);
        components.add(bodyComponent);

        template.put("components", components);
        payload.put("template", template);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
            return response.getBody();
        } catch (HttpStatusCodeException ex) {
            log.error("WhatsApp Message send failed with status {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new RuntimeException("WhatsApp Message API error (" + ex.getStatusCode() + "): " + ex.getResponseBodyAsString(), ex);
        }
    }

    /**
     * Creates a text parameter object with parameter_name for Meta WhatsApp named templates.
     */
    private Map<String, String> createTextParam(String parameterName, String text) {
        Map<String, String> param = new LinkedHashMap<>();
        param.put("type", "text");
        if (parameterName != null && !parameterName.isBlank()) {
            param.put("parameter_name", parameterName.trim());
        }
        param.put("text", text != null ? text : "");
        return param;
    }

    /**
     * Normalizes a phone number to WhatsApp international format (E.164 digits without leading '+').
     * Defaults to India (country code 91) if 10 digits are provided.
     */
    public String normalizePhoneNumber(String phone) {
        if (phone == null || phone.isBlank()) {
            return "";
        }
        // Remove all non-numeric characters (spaces, dashes, plus sign, brackets, etc.)
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.isBlank()) {
            return "";
        }

        // Handle 0091 international dialing prefix (e.g. 00918489140739 -> 918489140739)
        if (digits.startsWith("0091") && digits.length() == 14) {
            digits = digits.substring(2);
        }

        // Handle leading 0 trunk prefix (e.g. 08489140739 -> 8489140739)
        if (digits.startsWith("0") && digits.length() == 11) {
            digits = digits.substring(1);
        }

        // Standard 10-digit Indian mobile number -> prefix with country code 91 (e.g. 8489140739 -> 918489140739)
        if (digits.length() == 10) {
            return "91" + digits;
        }

        // Already in 12-digit format starting with 91 (e.g. 918489140739)
        if (digits.length() == 12 && digits.startsWith("91")) {
            return digits;
        }

        return digits;
    }

    private String formatAmount(Double amount) {
        if (amount == null) {
            return "0";
        }
        if (amount % 1 == 0) {
            return String.format("%.0f", amount);
        }
        return String.format("%.2f", amount);
    }

    private String sanitize(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }

    private void validateCredentials() {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalStateException("WhatsApp Access Token is not configured. Please set WHATSAPP_ACCESS_TOKEN in your environment or properties.");
        }
        if (phoneNumberId == null || phoneNumberId.isBlank()) {
            throw new IllegalStateException("WhatsApp Phone Number ID is not configured. Please set WHATSAPP_PHONE_NUMBER_ID in your environment or properties.");
        }
    }

    @SuppressWarnings("unchecked")
    private String extractMessageId(Map<String, Object> response) {
        if (response == null) {
            return null;
        }
        Object messagesObj = response.get("messages");
        if (messagesObj instanceof List<?> list && !list.isEmpty()) {
            Object first = list.get(0);
            if (first instanceof Map<?, ?> map) {
                Object id = map.get("id");
                return id != null ? id.toString() : null;
            }
        }
        return null;
    }
}
