package com.iskcondhanbad.donordash.dto;

import lombok.Data;
import java.util.Date;

@Data
public class SpecialDayDto {
    private Date date;
    private String purpose;
    private String otherPurpose;
}