package com.iskcondhanbad.donordash.dto;

import lombok.Data;

@Data
public class DonationSupervisorSignupRequestDto {
    private String email;
    private String mobileNumber;
    private String name;
    private String password;
    private String remark;
}
