package com.iskcondhanbad.donordash.service;

import com.iskcondhanbad.donordash.model.Donor;
import com.iskcondhanbad.donordash.model.DonorCultivator;
import com.iskcondhanbad.donordash.repository.DonorRepository;
import com.iskcondhanbad.donordash.dto.DonorDTO;
import com.iskcondhanbad.donordash.dto.DonorDetailsDto;
import com.iskcondhanbad.donordash.dto.DonorSignupDto;
import com.iskcondhanbad.donordash.repository.DonorCultivatorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;

@Service
public class DonorService {

    @Autowired
    DonorRepository donorRepository;

    @Autowired
    DonorCultivatorRepository donorCultivatorRepository;

    @Autowired
    SpecialDayService specialDayService;

    public Donor saveDonor(DonorSignupDto donorSignupDto) throws Exception {

        Donor donor = new Donor();
        Optional<DonorCultivator> donorCultivatorOptional = donorCultivatorRepository
                .findById(donorSignupDto.getDonorCultivatorId());

        if (!donorCultivatorOptional.isPresent()) {
            throw new RuntimeException("Donor Cultivator not found");
        }

        try {
            donor.setName(donorSignupDto.getName());
            donor.setUsername(donorSignupDto.getMobileNumber());
            donor.setCategory("Not Specified");
            donor.setType("One Timer");
            donor.setEmail(null);
            donor.setMobileNumber(donorSignupDto.getMobileNumber());
            donor.setPassword(donorSignupDto.getPassword());
            donor.setState(donorSignupDto.getState());
            donor.setCity(donorSignupDto.getCity());
            donor.setZone(donorSignupDto.getZone());
            donor.setPincode(donorSignupDto.getPincode());
            donor.setAddress(donorSignupDto.getAddress());
            donor.setPanNumber(donorSignupDto.getPanNumber());
            donor.setRemark(donorSignupDto.getRemark());
            donor.setDonorCultivator(donorCultivatorOptional.get());
            donor = donorRepository.save(donor);
            Integer donorId = donor.getId();
            donor.setUsername(generateUsername(donorSignupDto, donorId));
            donor = donorRepository.save(donor);
            return donor;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

    }

    public Donor updateDonor(Integer donorId, DonorSignupDto donorDto) throws Exception {
        Donor donor;
        try {
            donor = findDonorById(donorId);
        } catch (Exception e) {
            throw new Exception("Error finding donor with ID: " + donorId, e);
        }

        try {
            if (donorDto.getName() != null) {
                donor.setName(donorDto.getName());
            }
            if (donorDto.getEmail() != null) {
                donor.setEmail(donorDto.getEmail());
            }
            if (donorDto.getAddress() != null) {
                donor.setAddress(donorDto.getAddress());
            }
            if (donorDto.getCity() != null) {
                donor.setCity(donorDto.getCity());
            }
            if (donorDto.getState() != null) {
                donor.setState(donorDto.getState());
            }
            if (donorDto.getPincode() != null) {
                donor.setPincode(donorDto.getPincode());
            }
            if (donorDto.getPanNumber() != null) {
                donor.setPanNumber(donorDto.getPanNumber());
            }
            if (donorDto.getZone() != null) {
                donor.setZone(donorDto.getZone());
            }
            if (donorDto.getCategory() != null) {
                donor.setCategory(donorDto.getCategory());
            }
            if (donorDto.getRemark() != null) {
                donor.setRemark(donorDto.getRemark());
            }

            donorRepository.save(donor);
            specialDayService.updateSpecialDay(donorDto, donor);
            return donor;
        } catch (Exception e) {
            throw new Exception("Error updating donor with ID: " + donorId, e);
        }
    }

    public Donor findDonorById(Integer donorId) throws Exception {
        return donorRepository.findById(donorId)
                .orElseThrow(() -> new Exception("Donor not found with ID: " + donorId));
    }

    public List<DonorDTO> getDonors(Integer cultivatorId) {
        if (cultivatorId != null) {
            return donorRepository.findDonorsByCultivatorId(cultivatorId);
        } else {
            return donorRepository.findAllDonors();
        }
    }

    public Optional<Donor> getDonorById(Integer id) {
        return donorRepository.findById(id);
    }

    public String generateUsername(DonorSignupDto donorSignupDto, Integer id) {
        String namePart = donorSignupDto.getName().length() > 15
                ? donorSignupDto.getName().substring(0, 15)
                : donorSignupDto.getName();

        String addressPart = donorSignupDto.getAddress().length() > 15
                ? donorSignupDto.getAddress().substring(0, 15)
                : donorSignupDto.getAddress();

        return id + "," + namePart + "," + addressPart;
    }

    public List<DonorDetailsDto> getDonorsByCultivators(List<String> cultivatorNames) {
        List<Donor> donors = donorRepository
                .findByCultivatorNames(cultivatorNames == null || cultivatorNames.isEmpty() ? null : cultivatorNames);

        return donors.stream().map(d -> new DonorDetailsDto(
                d.getId(),
                d.getName(),
                d.getAddress() + ", " + d.getCity() + ", " + d.getState() + " - " + d.getPincode(),
                d.getMobileNumber(),
                d.getEmail(),
                d.getPanNumber(),
                d.getDonorCultivator() != null ? d.getDonorCultivator().getName() : null,
                d.getZone(),
                d.getPassword(),
                d.getCategory(),
                d.getType(),
                d.getUsername(),
                d.getRemark()
                )).collect(Collectors.toList());
    }

}