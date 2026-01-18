package com.iskcondhanbad.donordash.controller;

import com.iskcondhanbad.donordash.dto.*;

import com.iskcondhanbad.donordash.service.DonorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.iskcondhanbad.donordash.service.SpecialDayService;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("donor")
public class DonorController {

    @Autowired
    private final DonorService donorService;
    @Autowired
    private final SpecialDayService specialDayService;

    DonorController(DonorService donorService, SpecialDayService specialDayService) {
        this.donorService = donorService;
        this.specialDayService = specialDayService;
    }

    @PostMapping(value = "signup", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> signup(@RequestBody CreateDonorRequest createDonorRequest) {
        try {
            donorService.saveDonor(createDonorRequest);
            return ResponseEntity.ok(new ApiResponse(true," Donor registered successfully"));
        } catch (Exception ex) {
            return ResponseEntity
                    .badRequest()
                    .body(new ApiResponse(false, ex.getMessage()));
        }
    }

    @PutMapping(value = "edit/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ApiResponse> updateDonor(
            @PathVariable Integer id,
            @RequestBody CreateDonorRequest donorDto) {
        try {
            donorService.updateDonor(id, donorDto);
            return ResponseEntity.ok(new ApiResponse(true, "Donor updated successfully"));
        } catch (Exception ex) {
            return ResponseEntity
                    .badRequest()
                    .body(new ApiResponse(false, ex.getMessage()));
        }
    }


    @GetMapping(value = "by-id/{id}", produces = "application/json")
    public ResponseEntity<?> getDonorById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(donorService.getDonorById(id));
        } catch (Exception ex) {
            return ResponseEntity
                    .status(500)
                    .body(new ApiResponse(false, ex.getMessage()));
        }
    }

    @GetMapping(value = "/filter", produces = "application/json")
    public ResponseEntity<?> getDonors(
            @RequestParam(required = false) Integer donorCultivator) {
        try {
            return ResponseEntity.ok(donorService.getDonors(donorCultivator));
        } catch (Exception ex) {
            return ResponseEntity
                    .badRequest()
                    .body(new ApiResponse(false, ex.getMessage()));
        }
    }


    @GetMapping("/by-cultivator-names")
    public ResponseEntity<List<DonorDetailsDto>> getDonorsByCultivatorNames(
            @RequestParam(required = false) List<String> cultivatorNames) {
        List<DonorDetailsDto> donors = donorService.getDonorsByCultivators(cultivatorNames);
        return ResponseEntity.ok(donors);
    }

    @GetMapping(value = "/check-by-mobile", produces = "application/json")
    public ResponseEntity<?> checkDonorByMobile(@RequestParam String mobileNumber) {
        try {
            return ResponseEntity.ok(donorService.getDonorByMobile(mobileNumber));
        } catch (Exception ex) {
            return ResponseEntity
                    .status(500)
                    .body(new ApiResponse(false,"An error occurred while checking donor details"));
        }
    }

}