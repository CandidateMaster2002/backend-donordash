package com.iskcondhanbad.donordash.controller;

import com.iskcondhanbad.donordash.dto.LoginRequestDto;
import com.iskcondhanbad.donordash.dto.LoginResponseDto;
import com.iskcondhanbad.donordash.service.AuthService;
import com.iskcondhanbad.donordash.service.SpecialDayService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/special-day")
public class SpecialDayController {

    @Autowired
    SpecialDayService specialDayService;

    @GetMapping("/donor-id/{donorId}")
    public ResponseEntity<?> getAllSpecialDaysForDonor(@PathVariable Integer donorId) {
        try {
            return ResponseEntity.ok(specialDayService.getAllSpecialDaysByDonorId(donorId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An error occurred while fetching special days.");
        }
    }

    
}