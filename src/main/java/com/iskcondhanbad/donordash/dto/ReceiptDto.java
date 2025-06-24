package com.iskcondhanbad.donordash.dto;



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
    private String purpose;
    private String donorCultivatorName;
    private String donorCultivatorId;
}