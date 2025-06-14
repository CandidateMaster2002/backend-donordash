package com.iskcondhanbad.donordash.dto;
import lombok.Data;
import java.util.Date;

@Data
public class DonationDetailsDTO {
    private Date paymentDate;
    private Double donationAmount;
    private String donorName;
    private String pincode;
    private String fullAddress;
    private String panNumber;
    private String mobile;
    private String email;
    private String donationPurpose;
    private String paymentMethod;
    private String transactionId;
    private String remark;
    private String connectedTo;
    private String zone;
    private Date createdAt;
    private Date verifiedAt;
    private String receiptNumber;
}