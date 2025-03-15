package com.iskcondhanbad.donordash.service;

import com.iskcondhanbad.donordash.dto.DonorSignupDto;
import com.iskcondhanbad.donordash.model.Donor;
import com.iskcondhanbad.donordash.repository.SpecialDayRepository;
import com.iskcondhanbad.donordash.model.SpecialDay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
public class SpecialDayService {

    @Autowired
    SpecialDayRepository specialDayRepository;



    public List<SpecialDay> getAllSpecialDaysByDonorId(Integer donorId) {
        try {
            List<SpecialDay> specialDays = specialDayRepository.findAllByDonorId(donorId);
            if (specialDays.isEmpty()) {
                throw new Exception("SpecialDay not found for id: " + donorId);
            }
            return specialDays;
        } catch (Exception e) {
            // Log the exception
            System.err.println(e.getMessage());
            return null;
        }
    }

    public void saveSpecialDay(DonorSignupDto donorSignupDto, Donor donor) throws Exception {
        if(donorSignupDto.getWantPrasadam()==false) return;

        if (donorSignupDto.getSpecialDays() != null) {
            donorSignupDto.getSpecialDays().forEach(specialDayDto -> {
            if (specialDayDto.getDate() != null) {
            SpecialDay specialDay = new SpecialDay();
            specialDay.setDonor(donor);
            specialDay.setDate(specialDayDto.getDate());
            specialDay.setPurpose(specialDayDto.getPurpose());
            specialDay.setOtherPurpose(specialDayDto.getOtherPurpose());
            specialDayRepository.save(specialDay);
                }
            });
        }
    }

    public void updateSpecialDay(DonorSignupDto donorSignupDto, Donor donor) throws Exception {
        List<SpecialDay> savedSpecialDays = specialDayRepository.findAllByDonorId(donor.getId());
        if (!savedSpecialDays.isEmpty()) {
            specialDayRepository.deleteAll(savedSpecialDays);
        }
        saveSpecialDay(donorSignupDto, donor);
    }


  

  

     
}