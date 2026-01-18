package com.iskcondhanbad.donordash.service;

import com.iskcondhanbad.donordash.repository.DonorCultivatorRepository;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import com.iskcondhanbad.donordash.model.StoredDonorCultivator;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DonorCultivatorService {

    @Autowired
    DonorCultivatorRepository donorCultivatorRepository;

    @Transactional(readOnly = true)
    public List<StoredDonorCultivator> getAllDonorCultivators() {
        List<StoredDonorCultivator> donorCultivators = donorCultivatorRepository.findAll();
        if (donorCultivators.isEmpty()) {
            throw new RuntimeException("No donor cultivators found.");
        }
        return donorCultivators;
    }


    public StoredDonorCultivator changeDonationsVerified(Integer donorCultivatorId,Integer newCount) throws Exception {
        StoredDonorCultivator donorCultivator = donorCultivatorRepository.findById(donorCultivatorId)
            .orElseThrow(() -> new Exception("Donor Cultivator not found with ID: " + donorCultivatorId));
        donorCultivator.setDonationsVerified(newCount); 
        donorCultivatorRepository.save(donorCultivator);
        return donorCultivator;
    }

}