package com.iskcondhanbad.donordash.dto;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class DonationUpdateErrorDto {
   private Long donationId;
    private String errorMessage;
}

