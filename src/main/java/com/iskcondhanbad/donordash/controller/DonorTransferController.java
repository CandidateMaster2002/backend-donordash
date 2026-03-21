package com.iskcondhanbad.donordash.controller;

import com.iskcondhanbad.donordash.service.DonorTransferService;
import com.iskcondhanbad.donordash.dto.DonorTransferDto;
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

    

  @PostMapping("/acquire")
public ResponseEntity<ApiResponseDto<?>> requestToAcquire(
        @RequestParam Integer donorId,
        @RequestParam Integer requestedById) {
    try {
        DonorTransferDto transfer = donorTransferService.requestToAcquire(donorId, requestedById);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Acquire request submitted successfully", transfer));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(
            new ApiResponseDto<>(false,  e.getMessage())
        );
    }
}


@PostMapping("/release")
public ResponseEntity<ApiResponseDto<?>> requestToRelease(
        @RequestParam Integer donorId,
        @RequestParam Integer fromId,
        @RequestParam Integer toId) {
    try {
        DonorTransferDto transfer = donorTransferService.requestToRelease(donorId, fromId, toId);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Release request submitted successfully", transfer));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(
            new ApiResponseDto<>(false, "Failed to request release: " + e.getMessage())
        );
    }
}


  @PostMapping("/approve-acquire")
public ResponseEntity<ApiResponseDto<?>> approveAcquire(
        @RequestParam Integer donorId,
        @RequestParam Integer fromId,
        @RequestParam Integer toId) {
    try {
        DonorTransferDto transfer = donorTransferService.approveAcquire(donorId, fromId, toId);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Donor successfully transferred to acquiring cultivator.", transfer));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(
            new ApiResponseDto<>(false, "Failed to approve acquire: " + e.getMessage())
        );
    }
}

  @PostMapping("/approve-release")
public ResponseEntity<ApiResponseDto<?>> approveRelease(
        @RequestParam Integer donorId,
        @RequestParam Integer fromId,
        @RequestParam Integer toId) {
    try {
        DonorTransferDto transfer = donorTransferService.approveRelease(donorId, fromId, toId);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Donor successfully transferred to receiving cultivator.", transfer));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(
            new ApiResponseDto<>(false, "Failed to approve release: " + e.getMessage())
        );
    }
}

@GetMapping("/pending/{cultivatorId}")
public ResponseEntity<?> getPendingRequests(@PathVariable Integer cultivatorId) {
    try {
        List<DonorTransferDto> pending = donorTransferService.getPendingRequestsByCultivator(cultivatorId);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Fetched pending requests", pending));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ApiResponseDto<>(false, "Failed to fetch pending requests: " + e.getMessage()));
    }
}

}
