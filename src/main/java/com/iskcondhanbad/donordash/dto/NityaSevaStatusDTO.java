package com.iskcondhanbad.donordash.dto;
import lombok.Data;


@Data
public class NityaSevaStatusDTO {
    private Long id;
    private Integer donorId;
    private String donorName;
    private String month;
    private boolean sweetOrBtg;
    private boolean nityaSeva;
}