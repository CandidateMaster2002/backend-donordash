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
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        Admin admin = adminRepository.findByEmail(email);
        if (admin != null) {
            if (admin.getPassword().equals(password)) {
                return new LoginResponseDto("admin", admin);
            } else {
                throw new Exception("Incorrect password for admin.");
            }
        }

        DonationSupervisor supervisor = donationSupervisorRepository.findByEmail(email);
        if (supervisor != null) {
            if (supervisor.getPassword().equals(password)) {
                return new LoginResponseDto("donationSupervisor", supervisor);
            } else {
                throw new Exception("Incorrect password for donation supervisor.");
            }
        }

        DonorCultivator cultivator = donorCultivatorRepository.findByEmail(email);
        if (cultivator != null) {
            if (cultivator.getPassword().equals(password)) {
                return new LoginResponseDto("donorCultivator", cultivator);
            } else {
                throw new Exception("Incorrect password for donor cultivator.");
            }
        }

        Donor donor = donorRepository.findByEmail(email);
        if (donor != null) {
            if (donor.getPassword().equals(password)) {
                return new LoginResponseDto("donor", donor);
            } else {
                throw new Exception("Incorrect password for donor.");
            }
        }

        throw new Exception("Email does not exist.");
    }
}