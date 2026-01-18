package com.iskcondhanbad.donordash.dto;
import lombok.Data;

@Data
public class RegisteredDonorDetailsDto {
    private boolean donorRegistered;
    private Integer id;
    private String name;
    private String mobileNumber;
    private String state;
    private String city;
    private String pincode;
    private String address;
    private String donorCultivatorName;
}

