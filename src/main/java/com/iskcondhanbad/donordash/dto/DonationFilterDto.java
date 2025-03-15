package com.iskcondhanbad.donordash.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class DonationFilterDto {
    private Integer donationSupervisorId;
    private double minAmount;
    private double maxAmount;
    private Date fromDate;
    private Date toDate;
    private Integer donorId;
    private List<Integer> donorIds; 
    private Integer donorCultivatorId;
    private String status;
    private String paymentMode;
}