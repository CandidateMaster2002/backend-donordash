package com.iskcondhanbad.donordash.service;

import com.iskcondhanbad.donordash.dto.*;
import com.iskcondhanbad.donordash.model.StoredDonor;
import com.iskcondhanbad.donordash.model.StoredDonorCultivator;
import com.iskcondhanbad.donordash.repository.DonorRepository;
import com.iskcondhanbad.donordash.repository.DonorCultivatorRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.stream.Collectors;
import java.util.List;

@Service
public class DonorService {

    @Autowired
    DonorRepository donorRepository;

    @Autowired
    DonorCultivatorRepository donorCultivatorRepository;

    @Autowired
    SpecialDayService specialDayService;

    @Transactional
    public void saveDonor(CreateDonorRequest createDonorRequest) {

        StoredDonorCultivator cultivator = donorCultivatorRepository
                .findById(createDonorRequest.getDonorCultivatorId())
                .orElseThrow(() -> new RuntimeException("Donor cultivator not found"));

        StoredDonor donor = new StoredDonor();

        donor.setName(createDonorRequest.getName());
        donor.setCategory("Not Specified");
        donor.setType("One Timer");
        donor.setEmail(isBlank(createDonorRequest.getEmail()) ? null : createDonorRequest.getEmail());
        donor.setMobileNumber(createDonorRequest.getMobileNumber());
        donor.setPassword(createDonorRequest.getPassword());
        donor.setState(createDonorRequest.getState());
        donor.setCity(createDonorRequest.getCity());
        donor.setZone(createDonorRequest.getZone());
        donor.setPincode(createDonorRequest.getPincode());
        donor.setAddress(createDonorRequest.getAddress());
        donor.setPanNumber(createDonorRequest.getPanNumber());
        donor.setRemark(createDonorRequest.getRemark());
        donor.setDonorCultivator(cultivator);
        donor.setUsername(generateUsername(createDonorRequest.getName(),createDonorRequest.getMobileNumber()));
        donorRepository.save(donor);
    }

    @Transactional
    public void updateDonor(Integer donorId, CreateDonorRequest donorDto) throws Exception {
        try {
            StoredDonor donor = donorRepository.findById(donorId)
                    .orElseThrow(() -> new RuntimeException("Donor not found with ID: " + donorId));

            // Update fields only if they are not null/empty
            if (StringUtils.hasText(donorDto.getName())) donor.setName(donorDto.getName());
            donor.setEmail(StringUtils.hasText(donorDto.getEmail()) ? donorDto.getEmail() : null);
            if (StringUtils.hasText(donorDto.getAddress())) donor.setAddress(donorDto.getAddress());
            if (StringUtils.hasText(donorDto.getCity())) donor.setCity(donorDto.getCity());
            if (StringUtils.hasText(donorDto.getState())) donor.setState(donorDto.getState());
            if (donorDto.getPincode() != null) donor.setPincode(donorDto.getPincode());
            donor.setPanNumber(StringUtils.hasText(donorDto.getPanNumber()) ? donorDto.getPanNumber() : null);
            if (StringUtils.hasText(donorDto.getZone())) donor.setZone(donorDto.getZone());
            if (StringUtils.hasText(donorDto.getCategory())) donor.setCategory(donorDto.getCategory());
            if (StringUtils.hasText(donorDto.getRemark())) donor.setRemark(donorDto.getRemark());

            // Update special days if provided
            if (donorDto.getSpecialDays() != null && !donorDto.getSpecialDays().isEmpty()) {
                specialDayService.updateSpecialDay(donorDto, donor);
            }
        } catch (Exception e) {
            throw new Exception("Error updating donor with ID: " + donorId, e);
        }
    }


    @Transactional(readOnly = true)
    public List<Donor> getDonors(Integer cultivatorId) {
        return donorRepository.findDonors(cultivatorId);
    }

    @Transactional(readOnly = true)
    public Donor getDonorById(Integer id) {
        return donorRepository.findDonorById(id)
                .orElseThrow(() -> new RuntimeException("Donor not found with id: " + id));
    }


//    public String generateUsername(CreateDonorRequest createDonorRequest, Integer id) {
//        String namePart = createDonorRequest.getName().length() > 15
//                ? createDonorRequest.getName().substring(0, 15)
//                : createDonorRequest.getName();
//
//        String addressPart = createDonorRequest.getAddress().length() > 15
//                ? createDonorRequest.getAddress().substring(0, 15)
//                : createDonorRequest.getAddress();
//
//        return id + "," + namePart + "," + addressPart;
//    }

    public List<DonorDetailsDto> getDonorsByCultivators(List<String> cultivatorNames) {
        List<StoredDonor> donors = donorRepository
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

    @Transactional(readOnly = true)
    public RegisteredDonorDetailsDto getDonorByMobile(String mobileNumber) {

        StoredDonor donor = donorRepository.findByMobileNumber(mobileNumber);

        if (donor == null) {
            RegisteredDonorDetailsDto dto = new RegisteredDonorDetailsDto();
            dto.setDonorRegistered(false);
            return dto;
        }

        RegisteredDonorDetailsDto dto = new RegisteredDonorDetailsDto();
        dto.setDonorRegistered(true);
        dto.setId(donor.getId());
        dto.setName(donor.getName());
        dto.setMobileNumber(donor.getMobileNumber());
        dto.setState(donor.getState());
        dto.setCity(donor.getCity());
        dto.setPincode(donor.getPincode());
        dto.setAddress(donor.getAddress());
        dto.setDonorCultivatorName(donor.getDonorCultivator().getName());

        return dto;
    }


    private Donor toDomain(StoredDonor storedDonor) {
        if (storedDonor == null) return null;

        return Donor.builder()
                .donorName(storedDonor.getName())
                .wantPrasadam(false)
                .category(storedDonor.getCategory())
                .mobileNumber(storedDonor.getMobileNumber())
                .email(storedDonor.getEmail())
                .address(storedDonor.getAddress())
                .city(storedDonor.getCity())
                .state(storedDonor.getState())
                .pincode(storedDonor.getPincode())
                .panNumber(storedDonor.getPanNumber())
                .cultivatorName(null)
                .zone(storedDonor.getZone())
                .remark(storedDonor.getRemark())
                .type(storedDonor.getType())
                .username(storedDonor.getUsername())
                .supervisorName(null)
                .build();
    }

    public String generateUsername(String name, String mobileNumber) {
        if (name == null || name.isBlank() || mobileNumber == null || mobileNumber.isBlank()) {
            throw new IllegalArgumentException("Name and mobile number must not be empty");
        }

        String firstWord = name.trim().split("\\s+")[0].toLowerCase();
        return firstWord + "-" + mobileNumber.trim();
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }


}