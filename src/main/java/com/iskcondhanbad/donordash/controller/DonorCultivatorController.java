package com.iskcondhanbad.donordash.controller;

import com.iskcondhanbad.donordash.dto.ApiResponse;
import com.iskcondhanbad.donordash.service.DonorCultivatorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/donor-cultivator")
public class DonorCultivatorController {


@Autowired
DonorCultivatorService donorCultivatorService;

    @GetMapping(value = "/all", produces = "application/json")
    public ResponseEntity<?> getAllDonorCultivators() {
        try {
            return ResponseEntity.ok(donorCultivatorService.getAllDonorCultivators());
        } catch (Exception ex) {
            return ResponseEntity
                    .badRequest()
                    .body(new ApiResponse(false, ex.getMessage()));
        }
    }

}