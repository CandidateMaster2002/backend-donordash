package com.iskcondhanbad.donordash.dto;

import lombok.Data;

@Data
public class NityaSevaMonthStatusDTO {    
    public NityaSevaMonthStatusDTO(String month2, boolean sweetOrBtg2, boolean nityaSeva2) {
        this.month = month2;
        this.sweetOrBtg = sweetOrBtg2;
        this.nityaSeva = nityaSeva2;
    }
    private String month;
    private boolean sweetOrBtg;
    private boolean nityaSeva;
}
