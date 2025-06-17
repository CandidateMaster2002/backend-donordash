package com.iskcondhanbad.donordash.dto;

import lombok.Data;

@Data
public class DonorDetailsDto {
    public DonorDetailsDto(Integer donorId, String name, String fullAddress, String mobileNumber, String email,
            String panNumber, String connectedTo, String zone, String password,String category, String type, String username,String remark) {
        this.donorId = donorId;
        this.donorName = name;
        this.fullAddress = fullAddress;
        this.mobileNumber = mobileNumber;
        this.email = email;
        this.panNumber = panNumber;
        this.connectedTo = connectedTo;
        this.zone = zone;
        this.password = password;
        this.category = category;
        this.type = type;
        this.username = username;
        this.remark = remark;
    }

    private Integer donorId;
    private String donorName;
    private String fullAddress; // address + city + state + pincode
    private String mobileNumber;
    private String email;
    private String panNumber;
    private String connectedTo; // Cultivator name
    private String zone;
    private String password;
    private String category;
    private String type; // e.g., "One Timer", "Recurring"
    private String username; // Mobile number or email
    private String remark;
}
