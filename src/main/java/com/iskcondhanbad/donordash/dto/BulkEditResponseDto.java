package com.iskcondhanbad.donordash.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.List;
import com.iskcondhanbad.donordash.model.StoredDonation;

@Data
@AllArgsConstructor
public class BulkEditResponseDto{
    private List<StoredDonation> successfulUpdates;
    private List<DonationUpdateErrorDto> errors;
}
