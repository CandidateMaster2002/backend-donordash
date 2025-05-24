package com.iskcondhanbad.donordash.dto;
import lombok.Data;


@Data
public class UpdateNityaSevaStatusDTO {
    private Integer donorId;
    private String month;
    private String eventType; // "SweetOrBtg" or "NityaSeva"
    private boolean finalValue;
}