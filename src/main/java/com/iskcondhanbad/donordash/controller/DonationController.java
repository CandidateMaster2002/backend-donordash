package com.iskcondhanbad.donordash.controller;

import com.iskcondhanbad.donordash.dto.*;
import com.iskcondhanbad.donordash.model.StoredDonation;
import com.iskcondhanbad.donordash.service.DonationService;
import com.iskcondhanbad.donordash.service.RazorpayService;
import com.iskcondhanbad.donordash.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@RestController
@RequestMapping("/donation")
public class DonationController {

    @Autowired
    private DonationService donationService;

    @Autowired
    private RazorpayService razorpayService;

    @PostMapping("/donate")
    public ResponseEntity<?> donate(@RequestBody CreateDonationRequest createDonationRequest) {
        try {
            String paymentMode = createDonationRequest.getPaymentMode();
            if (isPaymentModeRequiringDuplicateCheck(paymentMode, createDonationRequest)) {
                validateNoDuplicateDonation(createDonationRequest);
            }
            AddDonationResponseDto addDonationResponseDto = donationService.donate(createDonationRequest);
            return ResponseEntity.ok(new ApiResponseDto<>(true, "Donation added successfully", addDonationResponseDto));
        } catch (IllegalArgumentException e) {
            log.error("Validation error in donate: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponseDto<>(false, e.getMessage()));
        } catch (Exception e) {
            log.error("Error in donate endpoint: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>(false, "Failed to add donation: " + e.getMessage()));
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

    @PutMapping(value = "/bulk-update", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> bulkEditDonations(@RequestBody List<UpdateDonationRequest> donationUpdates) {
        try {
            return ResponseEntity.ok(donationService.bulkEditDonations(donationUpdates));
        } catch (IllegalArgumentException ex) {
            log.error("Validation error in bulkEditDonations: {}", ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, ex.getMessage()));
        } catch (Exception ex) {
            log.error("Error in bulkEditDonations endpoint: ", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "An unexpected error occurred"));
        }
    }

    @PutMapping(value = "/edit/{donationId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> editDonation(@PathVariable Long donationId,
                                          @RequestBody UpdateDonationRequest request) {
        try {
            request.setDonationId(donationId);
            return ResponseEntity.ok(donationService.bulkEditDonations(List.of(request)));
        } catch (Exception ex) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, ex.getMessage()));
        }
    }

    @PostMapping(value = "/filter", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> getDonationsByFilter(@RequestBody DonationFilterDto filter) {
        try {
            return ResponseEntity.ok(donationService.getDonationsByFilter(filter, Function.identity()));
        } catch (Exception ex) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, ex.getMessage()));
        }
    }

    @GetMapping("/filtered")
    public ResponseEntity<?> getFilteredDonations(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                                                 @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
                                                 @RequestParam(required = false) List<String> paymentModes,
                                                 @RequestParam(required = false) List<String> statuses,
                                                 @RequestParam(required = false) List<String> cultivatorNames) {
        try {
            List<DonationDetailsDTO> donations = donationService.getFilteredDonations(startDate, endDate, paymentModes,
                    statuses, cultivatorNames);
            return ResponseEntity.ok(donations);
        } catch (IllegalArgumentException ex) {
            log.error("Validation error in getFilteredDonations: {}", ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, ex.getMessage()));
        } catch (Exception ex) {
            log.error("Error in getFilteredDonations endpoint: ", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "An unexpected error occurred"));
        }
    }

    @PostMapping(value = "/summary", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> getDonationSummary(@RequestBody SummaryRequest summaryRequest) {
        try {
            return ResponseEntity.ok(donationService.getDonationSummaryBy(summaryRequest));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "An unexpected error occurred"));
        }
    }

    // ===============================
    // Receipt and Utility Endpoints
    // ===============================

    @GetMapping("/receipt/{donationId}")
    public ResponseEntity<?> getReceipt(@PathVariable Long donationId) {
        try {
            ReceiptDto receipt = donationService.getReceipt(donationId);
            return ResponseEntity.ok(receipt);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/dummy")
    public ResponseEntity<?> postDummy() {
        try {
            return ResponseEntity.ok("Success");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private boolean isPaymentModeRequiringDuplicateCheck(String paymentMode,
                                                         CreateDonationRequest createDonationRequest) {
        return !Boolean.TRUE.equals(createDonationRequest.getIsUnclaimedRazorpayDonation()) && (Constants.BANK_TRANSFER.equals(paymentMode) ||
                Constants.RAZORPAY_LINK.equals(paymentMode));
    }

    /**
     * Validates that no duplicate donation exists in Razorpay for the given transaction ID
     * within a 30-day window around the payment date.
     *
     * @param createDonationRequest The donation request to validate
     * @throws IllegalArgumentException If a duplicate donation is found
     */
    private void validateNoDuplicateDonation(CreateDonationRequest createDonationRequest) throws Exception {
        Date paymentDate = createDonationRequest.getPaymentDate();
        String transactionId = createDonationRequest.getTransactionId();

        if (paymentDate == null || transactionId == null || transactionId.isEmpty()) {
            return;
        }

        Date fromDate = subtractDays(paymentDate, Constants.DUPLICATE_CHECK_DAYS);
        Date toDate = addDays(paymentDate, Constants.DUPLICATE_CHECK_DAYS);
        long fromTimestamp = fromDate.getTime() / 1000L;
        long toTimestamp = toDate.getTime() / 1000L;

        Map<String, Object> filters = new HashMap<>();
        filters.put(Constants.CAPTURED, true);

        List<RazorpayDonation> razorpayDonations = razorpayService.fetchDonations(fromTimestamp, toTimestamp, filters);

        boolean duplicateFound = razorpayDonations.stream()
                .anyMatch(donation -> transactionId.equals(donation.getRrn()) ||
                        transactionId.equals(donation.getTransactionId()));

        if (duplicateFound) {
            throw new IllegalArgumentException(
                    "A donation with transaction ID '" + transactionId + "' already exists in the database"
            );
        }
    }

    private Date subtractDays(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, -days);
        return calendar.getTime();
    }

    private Date addDays(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return calendar.getTime();
    }

}