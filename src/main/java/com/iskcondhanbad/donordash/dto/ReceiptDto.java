package com.iskcondhanbad.donordash.dto;

import com.iskcondhanbad.donordash.model.DonorCultivator;
import com.iskcondhanbad.donordash.model.SpecialDay;
import java.util.List;

import lombok.Data;

@Data
public class ReceiptDto {
    private String paymentDate;
    private double amount;
    private String donorName;
    private String donorAddress;
    private String donorPIN;
    private String pan;
    private String mobile;
    private String email;
    private String verifiedDate;
    private String paymentMode;
    private String transactionID;
    private String receiptNumber;
}