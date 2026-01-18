package com.iskcondhanbad.donordash.dto;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Donation {
    private Long id;
    private String donorName;
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
    private String donorCultivatorName;
    private String collectedByName;

    public Donation(
            Long id,
            String donorName,
            Double amount,
            String purpose,
            String paymentMode,
            String transactionId,
            String status,
            String remark,
            Integer donorId,
            Date createdAt,
            String receiptId,
            Date verifiedAt,
            Date paymentDate,
            String collectedByName,
            String donorCultivatorName
    ) {
        this.id = id;
        this.donorName = donorName;
        this.amount = amount;
        this.purpose = purpose;
        this.paymentMode = paymentMode;
        this.transactionId = transactionId;
        this.status = status;
        this.remark = remark;
        this.donorId = donorId;
        this.createdAt = createdAt;
        this.receiptId = receiptId;
        this.verifiedAt = verifiedAt;
        this.paymentDate = paymentDate;
        this.collectedByName = collectedByName;
        this.donorCultivatorName = donorCultivatorName;
    }

}

