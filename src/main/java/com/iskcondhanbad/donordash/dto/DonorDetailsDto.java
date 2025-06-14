package com.iskcondhanbad.donordash.dto;

import lombok.Data;

@Data
public class DonorDetailsDto {
    public DonorDetailsDto(String name, String fullAddress, String mobileNumber, String email,
            String panNumber, String connectedTo, String zone, String password) {
        this.donorName = name;
        this.fullAddress = fullAddress;
        this.mobileNumber = mobileNumber;
        this.email = email;
        this.panNumber = panNumber;
        this.connectedTo = connectedTo;
        this.zone = zone;
        this.password = password;
    }

    private String donorName;
    private String fullAddress; // address + city + state + pincode
    private String mobileNumber;
    private String email;
    private String panNumber;
    private String connectedTo; // Cultivator name
    private String zone;
    private String password;
}
