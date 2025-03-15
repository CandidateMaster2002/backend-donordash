package com.iskcondhanbad.donordash.dto;

import lombok.Data;

@Data
public class DonorCultivatorSignupRequestDto {
    private String email;
    private String imageSignature;
    private String mobileNumber;
    private String name;
    private String password;
    private String remark;
    private int donationSupervisorId;
}
