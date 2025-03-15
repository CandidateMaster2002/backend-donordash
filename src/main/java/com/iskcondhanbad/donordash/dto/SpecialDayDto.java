package com.iskcondhanbad.donordash.dto;

import com.iskcondhanbad.donordash.model.DonorCultivator;
import com.iskcondhanbad.donordash.model.SpecialDay;
import lombok.Data;
import java.util.Date;

@Data
public class SpecialDayDto {
    private Date date;
    private String purpose;
    private String otherPurpose;
}