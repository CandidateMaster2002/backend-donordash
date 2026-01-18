package com.iskcondhanbad.donordash.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Donor {
    private Integer donorId;
    private String donorName;
    private String category;
    private String mobileNumber;
    private String email;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String panNumber;
    private String cultivatorName;
    private String zone;
    private String remark;
    private String type;
    private String username;
    private boolean wantPrasadam;
    private String supervisorName;

    public Donor(Integer donorId,String donorName, String category, String mobileNumber, String email,
                 String address, String city, String state, String pincode, String panNumber,
                 String cultivatorName, String zone, String remark,String type,String username) {
        this.donorId = donorId;
        this.donorName = donorName;
        this.category = category;
        this.mobileNumber = mobileNumber;
        this.email = email;
        this.address = address;
        this.city = city;
        this.state = state;
        this.pincode = pincode;
        this.panNumber = panNumber;
        this.cultivatorName = cultivatorName;
        this.zone = zone;
        this.remark = remark;
        this.type = type;
        this.username = username;
    }

    public Donor(Integer donorId,String donorName, String category, String mobileNumber, String email,
                 String address, String city, String state, String pincode, String panNumber,
                 String cultivatorName, String zone, String remark,String type,String username,boolean wantPrasadam, String supervisorName) {
        this.donorId = donorId;
        this.donorName = donorName;
        this.wantPrasadam = wantPrasadam;
        this.category = category;
        this.mobileNumber = mobileNumber;
        this.email = email;
        this.address = address;
        this.city = city;
        this.state = state;
        this.pincode = pincode;
        this.panNumber = panNumber;
        this.cultivatorName = cultivatorName;
        this.zone = zone;
        this.remark = remark;
        this.type = type;
        this.username = username;
        this.supervisorName = supervisorName;
    }
}
