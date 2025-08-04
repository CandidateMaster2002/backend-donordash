package com.iskcondhanbad.donordash.dto;

import java.util.Date;

import lombok.Data;

@Data
public class DonationResponseDto {
    private Long id;
    private String donorName;
    private Double amount;
    private Date paymentDatDonationResponseDtoe;
    private String purpose;
    private String paymentMode;
    private String transactionId;
    private String status;
    private String remark;
    private Integer donorId;
    private Date createdAt;
    private Date paymentDate;
    private String receiptId;
    private Date verifiedAt;
    private String donorCultivatorName;
    private String collectedByName;
}

