package com.iskcondhanbad.donordash.controller;

import com.iskcondhanbad.donordash.dto.DonationDto;
import com.iskcondhanbad.donordash.dto.DonationFilterDto;
import com.iskcondhanbad.donordash.dto.DonationResponseDto;
import com.iskcondhanbad.donordash.model.Donation;
import com.iskcondhanbad.donordash.model.DonorCultivator;
import com.iskcondhanbad.donordash.service.DonationService;
import com.iskcondhanbad.donordash.service.DonorCultivatorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/donor-cultivator")
public class DonorCultivatorController {


@Autowired
DonorCultivatorService donorCultivatorService;


      @GetMapping("/all")
    public ResponseEntity<?> getAllDonorCultivators() {
        try {
            List<DonorCultivator> donorCultivators = donorCultivatorService.getAllDonorCultivators();
            return ResponseEntity.ok(donorCultivators);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An error occurred while fetching Donor Cultivators.");
        }
    }



   
}