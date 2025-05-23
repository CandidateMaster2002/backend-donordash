package com.iskcondhanbad.donordash.dto;


import java.util.List;

import lombok.Data;

@Data
public class DonorSignupDto {

    private String name;
    private String category;
    private String email;
    private String mobileNumber;
    private String password;
    private String state;
    private String city;
    private String zone;
    private String pincode;
    private String address;
    private String panNumber;
    private String remark;
    private List<SpecialDayDto> specialDays;
    private Integer donorCultivatorId;
}