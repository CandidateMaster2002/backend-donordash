package com.iskcondhanbad.donordash.dto;

import lombok.Data;

import java.util.Date;

@Data
public class EditDonationDto {
    private Double amount;
    private String purpose;
    private String paymentMode;
    private String transactionId;
    private String remark;
}