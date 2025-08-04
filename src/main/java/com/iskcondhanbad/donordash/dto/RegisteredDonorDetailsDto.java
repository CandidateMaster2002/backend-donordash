package com.iskcondhanbad.donordash.dto;
import lombok.Data;

@Data
public class RegisteredDonorDetailsDto {
    private boolean donorRegistered;
    private Integer id;
    private String name;
    private String username;
    private String type;
    private String category;
    private String email;
    private String mobileNumber;
    private String state;
    private String city;
    private String zone;
    private String pincode;
    private String address;
    private String panNumber;
    private String remark;
    private Integer donorCultivatorId;
    private String donorCultivatorName;
}

