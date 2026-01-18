package com.iskcondhanbad.donordash.service;

import com.iskcondhanbad.donordash.dto.LoginRequestDto;
import com.iskcondhanbad.donordash.dto.LoginResponseDto;
import com.iskcondhanbad.donordash.model.*;
import com.iskcondhanbad.donordash.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private DonationSupervisorRepository donationSupervisorRepository;

    @Autowired
    private DonorCultivatorRepository donorCultivatorRepository;

    @Autowired
    private DonorRepository donorRepository;

    public LoginResponseDto loginUser(LoginRequestDto loginRequest) throws Exception {
        String mobileNumber = loginRequest.getMobileNumber();
        String password = loginRequest.getPassword();

       System.out.println(mobileNumber);
        System.out.println(password);

        Admin admin = adminRepository.findByMobileNumber(mobileNumber);
        if (admin != null) {
            if (admin.getPassword().equals(password)) {
                return new LoginResponseDto("admin", admin);
            } else {
                throw new Exception("Incorrect password for admin.");
            }
        }

        DonationSupervisor supervisor = donationSupervisorRepository.findByMobileNumber(mobileNumber);
        if (supervisor != null) {
            if (supervisor.getPassword().equals(password)) {
                return new LoginResponseDto("donationSupervisor", supervisor);
            } else {
                throw new Exception("Incorrect password for donation supervisor.");
            }
        }

        StoredDonorCultivator cultivator = donorCultivatorRepository.findByMobileNumber(mobileNumber);
        if (cultivator != null) {
            if (cultivator.getPassword().equals(password)) {
                return new LoginResponseDto("donorCultivator", cultivator);
            } else {
                throw new Exception("Incorrect password for donor cultivator.");
            }
        }

        StoredDonor donor = donorRepository.findByMobileNumber(mobileNumber);
        if (donor != null) {
            if (donor.getPassword().equals(password)) {
                return new LoginResponseDto("donor", donor);
            } else {
                throw new Exception("Incorrect password for donor.");
            }
        }

        throw new Exception("Mobile no. does not exist.");
    }
}