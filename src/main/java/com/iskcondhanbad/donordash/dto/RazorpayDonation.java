package com.iskcondhanbad.donordash.dto;


import lombok.*;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RazorpayDonation {
    private String rrn;
    private Date paymentDate;
    private Double amount;
    private String transactionId;
    private String notes;
}
