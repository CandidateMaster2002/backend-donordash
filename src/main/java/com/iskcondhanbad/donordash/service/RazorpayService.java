package com.iskcondhanbad.donordash.service;

import com.iskcondhanbad.donordash.dto.RazorpayDonation;
import com.iskcondhanbad.donordash.utils.Constants;
import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RazorpayService {
    @Getter
    @Value("${razorpay.key-id}")
    private String razorpayKeyId;

    @Value("${razorpay.key-secret}")
    private String razorpayKeySecret;

    /**
     * Fetches donations from Razorpay based on the specified date range and filters.
     *
     * @param from   The start timestamp (Unix epoch in seconds)
     * @param to     The end timestamp (Unix epoch in seconds)
     * @param filters A map of key-value pairs to filter payments (e.g., "captured" -> true, "description" -> "QRv2 Payment")
     * @return List of RazorpayDonation objects matching the criteria
     * @throws Exception If there's an error communicating with Razorpay API
     */
    public List<RazorpayDonation> fetchDonations(long from, long to, Map<String, Object> filters) throws Exception {
        List<RazorpayDonation> result = new ArrayList<>();

        RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

        int count = 100;
        int skip = 0;

        while (true) {
            JSONObject request = new JSONObject();
            request.put("from", from);
            request.put("to", to);
            request.put("count", count);
            request.put("skip", skip);

            @SuppressWarnings("unchecked")
            List<Payment> payments = razorpayClient.Payments.fetchAll(request);

            for (Payment payment : payments) {
                // Check if the payment matches all the specified filters
                boolean matches = filters.entrySet().stream()
                        .allMatch(entry -> {
                            Object value = payment.get(entry.getKey());
                            return entry.getValue().equals(value);
                        });

                if (matches) {
                    Date paymentDate = extractTimestamp(payment.get(Constants.CAPTURED_AT));
                    String rrn = extractRrn(payment.get(Constants.ACQUIRER_DATA));
                    Double amount = extractAmount(payment.get(Constants.AMOUNT));
                    String notes = extractNotes(payment.get(Constants.NOTES));
                    String transactionId = payment.get(Constants.ID).toString();

                    result.add(RazorpayDonation.builder()
                            .rrn(rrn)
                            .paymentDate(paymentDate)
                            .amount(amount)
                            .transactionId(transactionId)
                            .notes(notes)
                            .build());
                }
            }

            if (payments.size() < count) {
                break;
            }
            skip += count;
        }

        return result;
    }

    private Date extractTimestamp(Object timestampObj) {
        if (timestampObj instanceof Date) {
            return (Date) timestampObj;
        } else if (timestampObj instanceof Number) {
            return new Date(((Number) timestampObj).longValue() * 1000);
        }
        return null;
    }

    //  Extracts amount from payment object (converts from paisa to rupees)
    private Double extractAmount(Object amountObj) {
        if (amountObj instanceof Number) {
            return ((Number) amountObj).doubleValue() / 100.0;
        }
        return null;
    }

    private String extractRrn(Object acquirerDataObj) {
        if (acquirerDataObj instanceof JSONObject) {
            return ((JSONObject) acquirerDataObj).optString("rrn", null);
        }
        return null;
    }

    private String extractNotes(Object notesObj) {
        if (notesObj instanceof JSONObject) {
            JSONObject notesJson = (JSONObject) notesObj;
            if (notesJson.length() > 0) {
                return notesJson.keys().next();
            }
        }
        return null;
    }

    public Order createOrder(Double amount, String currency, String receipt, long paymentCapture) throws Exception {
        RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amount);
        orderRequest.put("currency", currency);
        orderRequest.put("receipt", receipt);
        orderRequest.put("payment_capture", paymentCapture);

        Order order = razorpayClient.Orders.create(orderRequest);

        return order;
    }

    public boolean verifyPayment(String orderId, String paymentId, String signature) {
        String concatenatedString = orderId + "|" + paymentId;
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key_spec = new SecretKeySpec(razorpayKeySecret.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key_spec);

            byte[] hash = sha256_HMAC.doFinal(concatenatedString.getBytes());
            String calculatedSignature = Hex.encodeHexString(hash);

            return calculatedSignature.equals(signature);
        } catch (Exception e) {
            log.error("Error while verifying payment signature", e);
        }
        return false;
    }
}
