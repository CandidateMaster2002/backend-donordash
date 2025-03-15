package com.iskcondhanbad.donordash.dto;

import lombok.Data;

import java.util.Date;

@Data
public class DonationDto {
    private Double amount;
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
}