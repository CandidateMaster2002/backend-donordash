package com.iskcondhanbad.donordash.dto;

import lombok.Data;

import java.util.Date;

@Data
public class UpdateDonationRequest {
    private Long donationId;
    private Double amount;
    private String purpose;
    private String paymentMode;
    private String transactionId;
    private String remark;
    private String status;
    private String costCenter;
    private Date paymentDate;
}
