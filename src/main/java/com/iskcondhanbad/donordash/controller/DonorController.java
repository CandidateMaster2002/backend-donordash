package com.iskcondhanbad.donordash.controller;

import com.iskcondhanbad.donordash.model.Donor;

import com.iskcondhanbad.donordash.service.DonorService;
import com.iskcondhanbad.donordash.dto.DonorDetailsDto;
import com.iskcondhanbad.donordash.dto.DonorSignupDto;
import com.iskcondhanbad.donordash.dto.RegisteredDonorDetailsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.iskcondhanbad.donordash.service.SpecialDayService;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("donor")
public class DonorController {

    @Autowired
    DonorService donorService;
    @Autowired
    SpecialDayService specialDayService;

    DonorController(DonorService donorService, SpecialDayService specialDayService) {
        this.donorService = donorService;
        this.specialDayService = specialDayService;
    }

    @PostMapping("signup")
    public Donor signup(@RequestBody DonorSignupDto donorSignupDto) throws Exception {
        try {
            Donor donor = donorService.saveDonor(donorSignupDto);
            return donor;
        } catch (Exception e) {
            throw new RuntimeException("Error during donor signup", e);
        }
    }

    @PutMapping("edit/{id}")
    public ResponseEntity<?> updateDonor(@PathVariable Integer id, @RequestBody DonorSignupDto donorDto) {
        try {
            Donor updatedDonor = donorService.updateDonor(id, donorDto);
            return ResponseEntity.ok(updatedDonor);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("by-id/{id}")
    public ResponseEntity<?> getDonorById(@PathVariable Integer id) {
        try {
            Optional<Donor> donor = donorService.getDonorById(id);
            if (donor.isPresent()) {
                return ResponseEntity.ok(donor.get());
            } else {
                return ResponseEntity.status(404).body("Donor not found with ID: " + id);
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An error occurred while fetching the donor: " + e.getMessage());
        }
    }

    @GetMapping("/filter")
    public ResponseEntity<?> getDonors(@RequestParam(required = false) Integer donorCultivator) {
        try {
            List<DonorDetailsDto> donors = donorService.getDonors(donorCultivator);
            return ResponseEntity.ok(donors);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    

    @GetMapping("/by-cultivator-names")
    public ResponseEntity<List<DonorDetailsDto>> getDonorsByCultivatorNames(
            @RequestParam(required = false) List<String> cultivatorNames) {
        List<DonorDetailsDto> donors = donorService.getDonorsByCultivators(cultivatorNames);
        return ResponseEntity.ok(donors);
    }

    @GetMapping("/check-by-mobile")
    public ResponseEntity<RegisteredDonorDetailsDto> checkDonorByMobile(@RequestParam String mobileNumber) {
        RegisteredDonorDetailsDto dto = donorService.getDonorByMobile(mobileNumber);
        return ResponseEntity.ok(dto);
    }
}