package com.iskcondhanbad.donordash.controller;

import com.iskcondhanbad.donordash.dto.*;
import com.iskcondhanbad.donordash.model.StoredDonation;
import com.iskcondhanbad.donordash.service.DonationService;
import com.razorpay.RazorpayClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import com.razorpay.Order;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Slf4j
@RestController
@RequestMapping("/donation")
public class DonationController {

    @Autowired
    DonationService donationService;

      @PostMapping("/donate")
      public ResponseEntity<?> donate(@RequestBody CreateDonationRequest createDonationRequest) {
        try {
            AddDonationResponseDto addDonationResponseDto = donationService.donate(createDonationRequest);
            return ResponseEntity.ok(addDonationResponseDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
      }


    @GetMapping("/by-donor-id/{donorId}")
    public ResponseEntity<?> getDonationsByDonorId(@PathVariable Integer donorId) {
        try {
            List<StoredDonation> donations = donationService.getDonationsByDonorId(donorId);
            return ResponseEntity.ok(donations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/change-status/{donationId}/{newStatus}")
    public ResponseEntity<?> changeStatus(@PathVariable Long donationId, @PathVariable String newStatus) {
        try {
            StoredDonation donation = donationService.changeStatus(donationId, newStatus);
            return ResponseEntity.ok(donation);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/bulk-edit")
    public ResponseEntity<?> bulkEditDonations(@RequestBody List<EditDonationDtoWithId> donationUpdates) {
        BulkEditResponseDto response = donationService.bulkEditDonations(donationUpdates);
        return ResponseEntity.ok(response);
    }

    @PutMapping("edit/{donationId}")
    public ResponseEntity<?> editDonation(@PathVariable Long donationId, @RequestBody EditDonationDto editDonationDto) {
        try {
            StoredDonation donation = donationService.editDonation(donationId, editDonationDto);
            return ResponseEntity.ok(donation);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping(value = "/filter", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> getDonationsByFilter(@RequestBody DonationFilterDto filter) {
        try {
            return ResponseEntity.ok(donationService.getDonationsByFilter(filter));
        } catch (Exception ex) {
            return ResponseEntity
                    .badRequest()
                    .body(new ApiResponse(false, ex.getMessage()));
        }
    }


    @GetMapping("/summary")
    public ResponseEntity<Map<String, Double>> getDonationSummary(
            @RequestParam String parameter,
            @RequestParam(required = false) Integer cultivatorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date dateTo) {
        try {
            Map<String, Double> summary = donationService.getDonationSumBy(parameter, cultivatorId, dateFrom, dateTo);
            return ResponseEntity.ok(summary);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    
    @GetMapping("filtered")
    public ResponseEntity<List<DonationDetailsDTO>> getFilteredDonations(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @RequestParam(required = false) List<String> paymentModes,
            @RequestParam(required = false) List<String> statuses,
            @RequestParam(required = false) List<String> cultivatorNames) {
        List<DonationDetailsDTO> donations = donationService.getFilteredDonations(startDate, endDate, paymentModes,
                statuses,
                cultivatorNames);
        return ResponseEntity.ok(donations);
    }

    @GetMapping("/receipt/{donationId}")
    public ResponseEntity<?> getReceipt(@PathVariable Long donationId) {
        try {
            ReceiptDto receipt = donationService.getReceipt(donationId);
            return ResponseEntity.ok(receipt);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Value("${razorpay.keyId}")
    private String razorpayKeyId;

    @Value("${razorpay.keySecret}")
    private String razorpayKeySecret;

    @PostMapping("/create-order")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, Object> data) {
        try {
            RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", data.get("amount"));
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "txn_" + data.get("donorId"));
            orderRequest.put("payment_capture", 1);

            Order order = razorpayClient.Orders.create(orderRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("orderId", order.get("id"));
            response.put("amount", order.get("amount"));
            response.put("currency", order.get("currency"));
            response.put("key", razorpayKeyId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }


    @PostMapping("/verify-payment")
    public ResponseEntity<String> verifyPayment(@RequestBody Map<String, Object> data) {
        String razorpayOrderId = (String) data.get("razorpay_order_id");
        String razorpayPaymentId = (String) data.get("razorpay_payment_id");
        String razorpaySignature = (String) data.get("razorpay_signature");

        try {
            String generatedSignature = HmacSHA256(razorpayOrderId + "|" + razorpayPaymentId, razorpayKeySecret);

            if (generatedSignature.equals(razorpaySignature)) {
                // Payment is successful
                return ResponseEntity.ok("Payment Successful");
            } else {
                // Payment signature mismatch
                return ResponseEntity.status(400).body("Invalid Payment Signature");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Payment Verification Failed");
        }
    }

    @GetMapping("/dummy/{donorId}")
    public ResponseEntity<?> postDummy(@PathVariable Integer donorId) {
        try {
            return ResponseEntity.ok("Success");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private String HmacSHA256(String data, String key) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
        sha256_HMAC.init(secret_key);

        byte[] hash = sha256_HMAC.doFinal(data.getBytes("UTF-8"));
        return Hex.encodeHexString(hash);

    }

  
}