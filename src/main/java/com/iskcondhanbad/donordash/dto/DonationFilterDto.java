package com.iskcondhanbad.donordash.dto;

import lombok.*;

import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
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
    private Integer collectedById;
    private String status;
    private String paymentMode;
}