package com.iskcondhanbad.donordash.controller;

import com.iskcondhanbad.donordash.dto.ApiResponse;
import com.iskcondhanbad.donordash.dto.Donation;
import com.iskcondhanbad.donordash.dto.DonationFilterDto;
import com.iskcondhanbad.donordash.dto.RazorpayDonation;
import com.iskcondhanbad.donordash.service.DonationService;
import com.iskcondhanbad.donordash.service.RazorpayService;
import com.iskcondhanbad.donordash.utils.Constants;
import com.razorpay.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

@Slf4j
@RestController
@RequestMapping("/razorpay")
@RequiredArgsConstructor
public class RazorpayController {

    private final RazorpayService razorpayService;
    private final DonationService donationService;

    @PostMapping("/create-order")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, Object> data) {
        try {
            Double amount = Double.valueOf(data.get("amount").toString());
            String donorId = data.get("donorId").toString();

            Order order = razorpayService.createOrder(amount, "INR", "txn_" + donorId, 1);

            Map<String, Object> response = new HashMap<>();
            response.put("orderId", order.get("id"));
            response.put("amount", order.get("amount"));
            response.put("currency", order.get("currency"));
            response.put("key", razorpayService.getRazorpayKeyId());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating Razorpay order", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create order"));
        }
    }

    @PostMapping("/verify-payment")
    public ResponseEntity<String> verifyPayment(@RequestBody Map<String, Object> data) {
        String razorpayOrderId = (String) data.get("razorpay_order_id");
        String razorpayPaymentId = (String) data.get("razorpay_payment_id");
        String razorpaySignature = (String) data.get("razorpay_signature");

        try {
            boolean isValid = razorpayService.verifyPayment(razorpayOrderId, razorpayPaymentId, razorpaySignature);

            if (isValid) {
                return ResponseEntity.ok("Payment Successful");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Payment Signature");
            }
        } catch (Exception e) {
            log.error("Error verifying payment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Payment Verification Failed: " + e.getMessage());
        }
    }

    @GetMapping("/donations")
    public ResponseEntity<?> getDonations(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date from,
                                          @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date to) {
        try {
            long fromTimestamp = from.getTime() / 1000L;
            long toTimestamp = to.getTime() / 1000L;

            List<String> transactionIds = donationService.getDonationsByFilter(
                    DonationFilterDto.builder()
                            .fromDate(from)
                            .toDate(to)
                            .build(),
                    Donation::getTransactionId
            );

            Set<String> existingTransactionIds = new HashSet<>(transactionIds);

            Map<String, Object> filters = new HashMap<>();
            filters.put(Constants.CAPTURED, true);
            filters.put(Constants.DESCRIPTION, Constants.QR_V2_PAYMENT);

            List<RazorpayDonation> donations = razorpayService.fetchDonations(fromTimestamp, toTimestamp, filters)
                    .stream()
                    .filter(donation -> !existingTransactionIds.contains(donation.getTransactionId()) &&
                                       !existingTransactionIds.contains(donation.getRrn()))
                    .toList();

            return ResponseEntity.ok(donations);

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, ex.getMessage()));
        } catch (Exception ex) {
            log.error("Error fetching Razorpay donations", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to fetch donations"));
        }
    }
}
