package com.iskcondhanbad.donordash.service;

import com.iskcondhanbad.donordash.repository.DonorCultivatorRepository;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import com.iskcondhanbad.donordash.model.DonorCultivator;

@Service
public class DonorCultivatorService {

    @Autowired
    DonorCultivatorRepository donorCultivatorRepository;

    public List<DonorCultivator> getAllDonorCultivators() {
        try {
            List<DonorCultivator> donorCultivators = donorCultivatorRepository.findAll();
            if (donorCultivators.isEmpty()) {
                throw new RuntimeException("No Donor Cultivators found.");
            }
            return donorCultivators;
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while fetching Donor Cultivators: " + e.getMessage(), e);
        }
    }

    public DonorCultivator changeDonationsVerified(Integer donorCultivatorId,Integer newCount) throws Exception {
        DonorCultivator donorCultivator = donorCultivatorRepository.findById(donorCultivatorId)
            .orElseThrow(() -> new Exception("Donor Cultivator not found with ID: " + donorCultivatorId));
        donorCultivator.setDonationsVerified(newCount); 
        donorCultivatorRepository.save(donorCultivator);
        return donorCultivator;
    }

}