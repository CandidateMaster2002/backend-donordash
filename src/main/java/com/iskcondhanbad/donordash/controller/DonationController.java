package com.iskcondhanbad.donordash.controller;

import com.iskcondhanbad.donordash.dto.DonationDto;
import com.iskcondhanbad.donordash.dto.DonationFilterDto;
import com.iskcondhanbad.donordash.dto.DonationResponseDto;
import com.iskcondhanbad.donordash.dto.EditDonationDto;
import com.iskcondhanbad.donordash.dto.ReceiptDto;
import com.iskcondhanbad.donordash.model.Donation;
import com.iskcondhanbad.donordash.service.DonationService;
import com.razorpay.RazorpayClient;
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

@RestController
@RequestMapping("/donation")
public class DonationController {

    @Autowired
    DonationService donationService;

    @PostMapping("/donate")
    public ResponseEntity<?> donate(@RequestBody DonationDto donationDto) {
        try {
            Donation donation = donationService.donate(donationDto);
            return ResponseEntity.ok(donation);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/by-donor-id/{donorId}")
    public ResponseEntity<?> getDonationsByDonorId(@PathVariable Integer donorId) {
        try {
            List<Donation> donations = donationService.getDonationsByDonorId(donorId);
            return ResponseEntity.ok(donations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    

    @PutMapping("/change-status/{donationId}/{newStatus}")
    public ResponseEntity<?> changeStatus(@PathVariable Long donationId, @PathVariable String newStatus) {
        try {
            Donation donation = donationService.changeStatus(donationId, newStatus);
            return ResponseEntity.ok(donation);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("edit/{donationId}")
    public ResponseEntity<?> editDonation(@PathVariable Long donationId, @RequestBody EditDonationDto editDonationDto) {
        try {
            Donation donation = donationService.editDonation(donationId, editDonationDto);
            return ResponseEntity.ok(donation);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("filter")
    public ResponseEntity<?> getDonationsByFilter(@RequestBody DonationFilterDto filter) {
        try {
            List<DonationResponseDto> donations = donationService.getDonationsByFilter(filter);
            return ResponseEntity.ok(donations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
  @GetMapping("/summary")
    public ResponseEntity<Map<String, Double>> getDonationSummary(
            @RequestParam String parameter,
            @RequestParam(required = false) Integer cultivatorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date dateTo) 
            {
        try {
            Map<String, Double> summary = donationService.getDonationSumBy(parameter, cultivatorId, dateFrom, dateTo);
            return ResponseEntity.ok(summary);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
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

            Order order=razorpayClient.Orders.create(orderRequest);

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

    private String HmacSHA256(String data, String key) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
        sha256_HMAC.init(secret_key);

        byte[] hash = sha256_HMAC.doFinal(data.getBytes("UTF-8"));
        return Hex.encodeHexString(hash);
    }
  
}