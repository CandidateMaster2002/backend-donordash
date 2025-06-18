package com.iskcondhanbad.donordash.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.List;
import com.iskcondhanbad.donordash.model.Donation;

@Data
@AllArgsConstructor
public class BulkEditResponseDto{
    private List<Donation> successfulUpdates;
    private List<DonationUpdateErrorDto> errors;
}
