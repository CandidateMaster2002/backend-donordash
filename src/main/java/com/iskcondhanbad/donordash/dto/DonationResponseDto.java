package com.iskcondhanbad.donordash.dto;

import lombok.Data;

import java.util.Date;

@Data
public class DonationResponseDto {
    private Long id;
    private String donorName;
    private Double amount;
    private Date paymentDate;
    private String purpose;
    private String paymentMode;
    private String transactionId;
    private String status;
    private String remark;
    private Integer donorId;
    private Date createdAt;
    private String receiptId;
    private Date verifiedAt;
    private String donorCultivatorName;
}

