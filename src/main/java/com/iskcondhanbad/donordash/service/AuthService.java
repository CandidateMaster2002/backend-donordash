package com.iskcondhanbad.donordash.service;

import com.iskcondhanbad.donordash.dto.LoginRequestDto;
import com.iskcondhanbad.donordash.dto.LoginResponseDto;
import com.iskcondhanbad.donordash.model.*;
import com.iskcondhanbad.donordash.repository.*;
import com.iskcondhanbad.donordash.security.JwtUtil;
import com.iskcondhanbad.donordash.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
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

    @Autowired
    private JwtUtil jwtUtil;

    public LoginResponseDto loginUser(LoginRequestDto loginRequest) throws Exception {
        String mobileNumber = loginRequest.getMobileNumber();
        String password = loginRequest.getPassword();

        log.info("Login attempt for mobile number: {}", mobileNumber);

        Admin admin = adminRepository.findByMobileNumber(mobileNumber);
        if (admin != null) {
            if (admin.getPassword().equals(password)) {
                String token = jwtUtil.generateToken(admin.getMobileNumber(), admin.getId(), Constants.ADMIN, admin.getName());
                return new LoginResponseDto(Constants.ADMIN, admin, token);
            } else {
                throw new Exception("Incorrect password for admin.");
            }
        }

        DonationSupervisor supervisor = donationSupervisorRepository.findByMobileNumber(mobileNumber);
        if (supervisor != null) {
            if (supervisor.getPassword().equals(password)) {
                String token = jwtUtil.generateToken(supervisor.getMobileNumber(), supervisor.getId(), Constants.DONATION_SUPERVISOR, supervisor.getName());
                return new LoginResponseDto(Constants.DONATION_SUPERVISOR, supervisor, token);
            } else {
                throw new Exception("Incorrect password for donation supervisor.");
            }
        }

        StoredDonorCultivator cultivator = donorCultivatorRepository.findByMobileNumber(mobileNumber);
        if (cultivator != null) {
            if (cultivator.getPassword().equals(password)) {
                String token = jwtUtil.generateToken(cultivator.getMobileNumber(), cultivator.getId(), Constants.DONOR_CULTIVATOR, cultivator.getName());
                return new LoginResponseDto(Constants.DONOR_CULTIVATOR, cultivator, token);
            } else {
                throw new Exception("Incorrect password for donor cultivator.");
            }
        }

        StoredDonor donor = donorRepository.findByMobileNumber(mobileNumber);
        if (donor != null) {
            if (donor.getPassword().equals(password)) {
                String token = jwtUtil.generateToken(donor.getMobileNumber(), donor.getId(), Constants.DONOR, donor.getName());
                return new LoginResponseDto(Constants.DONOR, donor, token);
            } else {
                throw new Exception("Incorrect password for donor.");
            }
        }

        throw new Exception("Mobile no. does not exist.");
    }
}
