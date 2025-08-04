package com.iskcondhanbad.donordash.controller;

import com.iskcondhanbad.donordash.service.DonorTransferService;
import com.iskcondhanbad.donordash.model.DonorTransfer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.iskcondhanbad.donordash.dto.ApiResponseDto;


import java.util.List;

@RestController
@RequestMapping("donor-transfer")

public class DonorTransferController {

    @Autowired
    DonorTransferService donorTransferService;

    

    // 1. Request to Acquire
  @PostMapping("/acquire")
public ResponseEntity<ApiResponseDto<?>> requestToAcquire(
        @RequestParam Integer donorId,
        @RequestParam Integer requestedById) {
    try {
        DonorTransfer transfer = donorTransferService.requestToAcquire(donorId, requestedById);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Acquire request submitted successfully", transfer));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(
            new ApiResponseDto<>(false, "Failed to request acquire: " + e.getMessage())
        );
    }
}


    // 2. Request to Release
@PostMapping("/release")
public ResponseEntity<ApiResponseDto<?>> requestToRelease(
        @RequestParam Integer donorId,
        @RequestParam Integer fromId,
        @RequestParam Integer toId) {
    try {
        DonorTransfer transfer = donorTransferService.requestToRelease(donorId, fromId, toId);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Release request submitted successfully", transfer));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(
            new ApiResponseDto<>(false, "Failed to request release: " + e.getMessage())
        );
    }
}


    // 3. Approve Acquire
  @PostMapping("/approve-acquire")
public ResponseEntity<ApiResponseDto<?>> approveAcquire(
        @RequestParam Integer donorId,
        @RequestParam Integer fromId,
        @RequestParam Integer toId) {
    try {
        donorTransferService.approveAcquire(donorId, fromId, toId);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Donor successfully transferred to acquiring cultivator."));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(
            new ApiResponseDto<>(false, "Failed to approve acquire: " + e.getMessage())
        );
    }
}

    // 4. Approve Release
  @PostMapping("/approve-release")
public ResponseEntity<ApiResponseDto<?>> approveRelease(
        @RequestParam Integer donorId,
        @RequestParam Integer fromId,
        @RequestParam Integer toId) {
    try {
        donorTransferService.approveRelease(donorId, fromId, toId);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Donor successfully transferred to receiving cultivator."));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(
            new ApiResponseDto<>(false, "Failed to approve release: " + e.getMessage())
        );
    }
}

@GetMapping("/pending/{cultivatorId}")
public ResponseEntity<ApiResponseDto<?>> getPendingRequests(@PathVariable Integer cultivatorId) {
    try {
        List<DonorTransfer> pending = donorTransferService.getPendingRequestsByCultivator(cultivatorId);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Fetched pending requests", pending));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ApiResponseDto<>(false, "Failed to fetch pending requests: " + e.getMessage()));
    }
}

}
