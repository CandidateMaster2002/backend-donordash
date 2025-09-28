package com.iskcondhanbad.donordash.dto;

import lombok.Data;

import java.util.Date;

@Data
public class EditDonationDtoWithId {
    private Long donationId;
    private Double amount;
    private String purpose;
    private String paymentMode;
    private String transactionId;
    private String remark;
    private String status; // Optional
    private String costCenter;
}
